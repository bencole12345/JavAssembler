;; The garbage collector
(func $gc

  (local $new_heap_base i32)
  (local $new_shadow_stack_base i32)
  (local $curr_stack_offset i32)
  (local $curr_heap_offset i32)
  (local $relocated_address i32)

  ;; Compute where the new stack and heap will go
  global.get $gc_curr_heap_half
  call $_gc_next_heap_start
  local.set $new_heap_base
  global.get $gc_curr_heap_half
  call $_gc_next_shadow_stack_base
  local.set $new_shadow_stack_base

  ;; Loop over each pointer in the shadow stack
  (block
    (loop

      ;; if curr_stack_offset > shadow_stack_next_offset
      ;;    then break
      local.get $curr_stack_offset
      global.get $shadow_stack_next_offset
      i32.ge_u
      br_if 1

      ;; Look up the current item
      global.get $shadow_stack_base
      local.get $curr_stack_offset
      i32.const 4
      i32.mul
      i32.sub
      i32.load

      ;; Relocate this item
      call $_gc_move_at_address

      ;; Save the location it as moved to
      local.set $relocated_address

      ;; Figure out the corresponding location in the new stack
      local.get $new_shadow_stack_base
      local.get $curr_stack_offset
      i32.const 4
      i32.mul
      i32.sub

      ;; Write the relocated heap value there
      local.get $relocated_address
      i32.store
    )
  )

  ;; Set the new shadow stack base
  local.get $new_shadow_stack_base
  global.set $shadow_stack_base

)

(func $_gc_move_at_address
  (param $address i32)
  (result i32)
  
  (local $new_address i32)
  (local $is_object i32)
  (local $elements_bytes i32)
  (local $curr_pos_old_heap i32)
  (local $curr_pos_new_heap i32)
  (local $curr_offset i32)
  (local $attributes_end_pos_old_heap i32)
  (local $whole_block_end_old_heap i32)
  (local $space_to_allocate i32)

  ;; Check whether this address has already been visited
  local.get $address
  call $_gc_is_address_marked
  if
    local.get $address
    call $_gc_get_moved_to_address
    return
  end

  ;; Mark the current address
  local.get $address
  call $_gc_mark_address

  ;; Read the number of elements
  ;; For an array this will be 4*length; for an object this
  ;; will be the number of bytes needed for its attributes
  local.get $address
  i32.load offset=1
  local.set $elements_bytes

  ;; Determine whether we are looking at an object or an array
  local.get $address
  call $_gc_is_object
  local.set $is_object

  ;; If it's an object then add the pointer info bytes to the length
  local.get $is_object
  if
    ;; Object case
    local.get $address
    i32.const 9
    local.get $elements_bytes
    i32.add
    local.get $elements_bytes
    call $num_bytes_to_pointer_info_length
    i32.add
    i32.add
    local.set $whole_block_end_old_heap
  else
    ;; Array case
    local.get $address
    i32.const 5
    local.get $elements_bytes
    i32.add
    i32.add
    local.set $whole_block_end_old_heap
  end

  ;; Compute how much space we need to allocate
  local.get $whole_block_end_old_heap
  local.get $address
  i32.sub
  local.set $space_to_allocate

  ;; Allocate the space in the new heap
  global.get $next_free_space
  local.set $new_address
  global.get $next_free_space
  local.get $space_to_allocate
  i32.add
  global.set $next_free_space

  ;; Set the moved_to field
  local.get $address
  local.get $new_address
  i32.store offset=1

  ;; Now copy the stuff over
  local.get $is_object
  if
    ;; Object case

    ;; Write the flag (gc=0, is_object=1)
    local.get $new_address
    i32.const 0x01
    i32.store8

    ;; Write the size
    local.get $new_address
    local.get $elements_bytes
    i32.store offset=1

    ;; Write the vtable pointer
    local.get $new_address
    local.get $address
    i32.load offset=5
    i32.store offset=5

    ;; Start at the first attribute
    local.get $address
    i32.const 9
    i32.add
    local.set $curr_pos_old_heap
    local.get $new_address
    i32.const 9
    i32.add
    local.set $curr_pos_new_heap
    i32.const 0
    local.set $curr_offset

    ;; Compute the end of the attributes
    local.get $curr_pos_old_heap
    local.get $elements_bytes
    i32.add
    local.set $attributes_end_pos_old_heap
    
    ;; Copy over each attribute
    (block
      (loop

        ;; Break if we have reached the end of the attributes
        local.get $curr_pos_old_heap
        local.get $attributes_end_pos_old_heap
        i32.ge_u
        br_if 1

        ;; Put the address of this attribute in the new heap
        ;; on the stack
        local.get $curr_pos_new_heap

        ;; If it's a pointer, recursively move the object it points
        ;; to, and update the value on the stack to the new address
        local.get $address
        local.get $curr_offset
        call $_gc_is_pointer
        if (result i32)
          ;; Recursively move this object
          local.get $curr_pos_old_heap
          i32.load
          call $_gc_move_at_address
        else
          ;; Just copy it directly
          local.get $curr_pos_old_heap
          i32.load
        end

        ;; Write the value to the new heap address
        i32.store

        ;; Increment loop counters
        local.get $curr_pos_old_heap
        i32.const 4
        i32.add
        local.set $curr_pos_old_heap
        local.get $curr_pos_new_heap
        i32.const 4
        i32.add
        local.set $curr_pos_new_heap
        local.get $curr_offset
        i32.const 1
        i32.add
        local.set $curr_offset

      )
    )

    ;; Also move over the pointer info
    (block
      (loop

        ;; Break if we have reached the end of the object
        local.get $curr_pos_old_heap
        local.get $whole_block_end_old_heap
        i32.ge_u
        br_if 1

        ;; Copy the 32 bits of pointer info in this word to
        ;; the object in the new heap
        local.get $curr_pos_new_heap
        local.get $curr_pos_old_heap
        i32.load
        i32.store

        ;; Update loop variables
        local.get $curr_pos_old_heap
        i32.const 4
        i32.add
        local.set $curr_pos_old_heap
        local.get $curr_pos_new_heap
        i32.const 4
        i32.add
        local.set $curr_pos_new_heap
      )
    )


  else
    ;; Array case

    ;; Write the flag (gc=0, is_object=0)
    local.get $new_address
    i32.const 0x00
    i32.store8

    ;; Write the size
    local.get $new_address
    local.get $elements_bytes
    i32.store offset=1

    ;; Start at the first attribute
    local.get $address
    i32.const 5
    i32.add
    local.set $curr_pos_old_heap
    local.get $new_address
    i32.const 5
    i32.add
    local.set $curr_pos_new_heap

    ;; Compute the end of the attributes
    local.get $curr_pos_old_heap
    local.get $elements_bytes
    i32.add
    local.set $attributes_end_pos_old_heap
    
    ;; Copy over each attribute
    (block
      (loop
        ;; Break if we have reached the end of the attributes
        local.get $curr_pos_old_heap
        local.get $attributes_end_pos_old_heap
        i32.ge_u
        br_if 1

        ;; Load the new address for the object at this index
        local.get $curr_pos_new_heap

        ;; Load the pointer stored here and move that object first
        local.get $curr_pos_old_heap
        i32.load
        call $_gc_move_at_address

        ;; Write the value to the new heap address
        i32.store

        ;; Increment loop counters
        local.get $curr_pos_old_heap
        i32.const 4
        i32.add
        local.set $curr_pos_old_heap
        local.get $curr_pos_new_heap
        i32.const 4
        i32.add
        local.set $curr_pos_new_heap
      )
    )
  end

  ;; Return the new address of the object
  local.get $new_address

)

;; Determines whether a given address is an object
(func $_gc_is_object
  (param $address i32)
  (result i32)

  local.get $address
  i32.load8_u
  i32.const 0x01
  i32.and
)

;; Determine whether the attribute at a given offset
;; should be treated as a pointer
(func $_gc_is_pointer
  (param $address i32)
  (param $offset i32)
  (result i32)

  (local $address_to_read i32)
  (local $bit_to_read i32)

  ;; Use offset to work out which address to read
  local.get $address
  local.get $offset
  call $num_bytes_to_pointer_info_length
  i32.add
  local.set $address_to_read

  ;; Use offset to work out which bit to read
  local.get $offset
  i32.const 0x0000001f
  i32.and
  local.set $bit_to_read

  ;; Read the bit
  local.get $address_to_read
  i32.load
  local.get $bit_to_read
  i32.shr_u
  i32.const 1
  i32.and
  
)

;; Marks an object as having been processed
(func $_gc_mark_address
  (param $address i32)

  ;; Address to write to
  local.get $address

  ;; Value to write
  local.get $address
  i32.load8_u
  i32.const 0x02
  i32.or

  ;; Set the bit
  i32.store8
)

(func $_gc_is_address_marked
  (param $address i32)
  (result i32)

  local.get $address
  i32.load8_u
  i32.const 0x02
  i32.and
)

(func $_gc_get_moved_to_address
  (param $address i32)
  (result i32)

  local.get $address
  i32.load offset=1
)

(func $_gc_next_heap
  (param $current_heap i32)
  (result i32)
  local.get $current_heap
  i32.const 1
  i32.xor
)

(func $_gc_next_heap_start
  (param $current_heap i32)
  (result i32)
  local.get $current_heap
  if (result i32)
    i32.const 0
  else
    i32.const 32768
  end
  )

(func $_gc_next_shadow_stack_base
  (param $current_heap i32)
  (result i32)
  local.get $current_heap
  if (result i32)
    i32.const 32764
  else
    i32.const 65532
  end
)

(func $num_bytes_to_pointer_info_length
  (param $num_bytes i32)
  (result i32)

  local.get $num_bytes
  i32.const 31
  i32.add
  i32.const 5
  i32.shr_u
)
