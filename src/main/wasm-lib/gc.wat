;; The garbage collector
(func $gc
  (local $tospace_stack_base i32)
  (local $curr_stack_offset i32)
  (local $fromspace_object_address i32)
  (local $tospace_allocated_address i32)
  (local $currently_traced_to i32)
  (local $start_of_current_group i32)
  (local $current_object_being_traced i32)

  ;; Compute where the new heap will go
  global.get $curr_heap
  call $_gc_next_heap_start
  global.set $heap_last_allocated

  ;; Compute where the new stack will go
  global.get $curr_heap
  call $_gc_next_stack_base
  local.set $tospace_stack_base

  ;; Update the heap
  global.get $curr_heap
  call $_gc_next_heap
  global.set $curr_heap

  ;; Set $currently_traced_to
  global.get $heap_last_allocated
  local.set $currently_traced_to

  ;; Move each stack-accessible object
  (block (loop
    local.get $curr_stack_offset
    global.get $stack_pointer
    i32.ge_u
    br_if 1

    ;; Look up the current item
    global.get $stack_base
    local.get $curr_stack_offset
    i32.add
    i32.load
    local.tee $fromspace_object_address

    ;; Copy it across, saving its new address
    call $_gc_copy_object
    local.set $tospace_allocated_address

    ;; Write pointer to new stack
    local.get $tospace_stack_base
    local.get $curr_stack_offset
    i32.add
    local.get $tospace_allocated_address
    i32.store

    ;; Move to the next stack position
    local.get $curr_stack_offset
    i32.const 4
    i32.add
    local.set $curr_stack_offset
    br 0
  ))

  ;; Set the new shadow stack base
  local.get $tospace_stack_base
  global.set $stack_base

  (block (loop
    global.get $heap_last_allocated
    local.get $currently_traced_to
    i32.ge_u
    br_if 1

    global.get $heap_last_allocated
    local.set $start_of_current_group

    ;; Trace each object in the current group
    global.get $heap_last_allocated
    local.set $current_object_being_traced
    (block (loop
      local.get $current_object_being_traced
      local.get $currently_traced_to
      i32.ge_u
      br_if 1

      ;; Trace this item
      local.get $current_object_being_traced
      call $_gc_trace_object

      ;; Move to the next object in the current block
      local.get $current_object_being_traced
      call $_gc_determine_size
      local.get $current_object_being_traced
      i32.add
      local.set $current_object_being_traced
      br 0
    ))

    local.get $start_of_current_group
    local.set $currently_traced_to
    br 0
  ))
)


(func $_gc_trace_object
  (param $address i32)

  (local $size_field i32)
  (local $curr_attribute_offset i32)

  ;; Read size field
  local.get $address
  i32.load offset=1
  local.set $size_field

  ;; Determine whether it's an object or an array
  local.get $address
  call $_gc_is_object
  if
    ;; It's an object!

    (block (loop
      local.get $curr_attribute_offset
      local.get $size_field
      i32.ge_u
      br_if 1

      local.get $address
      local.get $curr_attribute_offset
      call $_gc_is_pointer
      if

        ;; Leave the index on the stack
        local.get $address
        local.get $curr_attribute_offset
        i32.add

        ;; Copy the object pointed to
        local.get $address
        local.get $curr_attribute_offset
        i32.add
        i32.load offset=9
        call $_gc_copy_object
        
        ;; Write its new address to the index we worked out earlier
        i32.store offset=9

      end

      local.get $curr_attribute_offset
      i32.const 4
      i32.add
      local.set $curr_attribute_offset
      br 0
    ))



  else
    ;; It's an array!

    ;; Determine whether it contains pointers
    local.get $address
    call $_gc_array_contains_pointers
    if
      (block (loop
        local.get $curr_attribute_offset
        local.get $size_field
        i32.ge_u
        br_if 1

        ;; Leave the index on the stack
        local.get $address
        local.get $curr_attribute_offset
        i32.add

        ;; Copy the object pointed to
        local.get $address
        local.get $curr_attribute_offset
        i32.add
        i32.load offset=5
        call $_gc_copy_object

        ;; Write the new address to the index we left on the stack earlier
        i32.store offset=5

        local.get $curr_attribute_offset
        i32.const 4
        i32.add
        local.set $curr_attribute_offset
        br 0
      ))
    end
  end
)


(func $_gc_copy_object
  (param $address i32)
  ;; Returns the new address
  (result i32)

  ;; Look at flag
  local.get $address
  call $_gc_is_address_marked
  if (result i32)
    local.get $address
    i32.load offset=1
  else
    local.get $address
    local.get $address
    call $_gc_determine_size
    call $_gc_copy_bytes
  end
)


(func $_gc_copy_bytes
  (param $from_address i32)
  (param $size_bytes i32)

  ;; Returns the address it allocates
  (result i32)

  (local $allocated_address i32)
  (local $curr_byte i32)

  ;; If it's a null pointer then don't copy anything, just return null
  local.get $from_address
  i32.eqz
  if
    i32.const 0
    return
  end

  ;; Allocate space for the object
  global.get $heap_last_allocated
  local.get $size_bytes
  i32.sub
  local.tee $allocated_address
  global.set $heap_last_allocated

  ;; Do the copy, one byte at a time
  (block (loop
    local.get $curr_byte
    local.get $size_bytes
    i32.ge_s
    br_if 1

    ;; Copy the byte across
    local.get $allocated_address
    local.get $curr_byte
    i32.add
    local.get $from_address
    local.get $curr_byte
    i32.add
    i32.load8_u
    i32.store8

    local.get $curr_byte
    i32.const 1
    i32.add
    local.set $curr_byte
    br 0
  ))

  ;; Write the new pointer to the old object's size field
  local.get $from_address
  local.get $allocated_address
  call $_gc_set_moved_to_address

  ;; Make sure the "was moved" bit is 0 in the tospace
  local.get $allocated_address
  local.get $allocated_address
  i32.load8_u
  i32.const 0xfd
  i32.and
  i32.store8

  ;; Return the allocated address
  local.get $allocated_address
)


;; Determines the size of an object in bytes
(func $_gc_determine_size
  (param $address i32)
  (result i32)
  (local $size_field i32)

  local.get $address
  call $_gc_is_object
  if (result i32)
    local.get $address
    i32.load offset=1
    local.tee $size_field
    call $num_bytes_to_pointer_info_length
    local.get $size_field
    i32.add
    i32.const 9
    i32.add
  else
    local.get $address
    i32.load offset=1
    i32.const 5
    i32.add
  end
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

  (local $size_field i32)
  (local $offset_within_pointers_section i32)
  (local $bit_to_read i32)

  ;; Read the size field
  local.get $address
  i32.load offset=1
  local.set $size_field

  ;; Work out which word within the pointers section to read
  local.get $offset
  i32.const 5
  i32.shr_u
  local.set $offset_within_pointers_section

  ;; Use offset to work out which bit to read
  local.get $offset
  i32.const 0x0000001f
  i32.and
  local.set $bit_to_read

  ;; Read the bit
  local.get $address
  local.get $size_field
  i32.add
  local.get $offset_within_pointers_section
  i32.add
  i32.load offset=9
  local.get $bit_to_read
  i32.shr_u
  i32.const 1
  i32.and
)


(func $_gc_is_address_marked
  (param $address i32)
  (result i32)

  local.get $address
  i32.load8_u
  i32.const 0x02
  i32.and
  i32.const 1
  i32.shr_u
)


(func $_gc_get_moved_to_address
  (param $address i32)
  (result i32)
  local.get $address
  i32.load offset=1
)


(func $_gc_set_moved_to_address
  (param $old_address i32)
  (param $new_address i32)

  ;; Write the bit to say it was moved
  local.get $old_address
  local.get $old_address
  i32.load8_u
  i32.const 0x02
  i32.or
  i32.store8

  ;; Write the new address
  local.get $old_address
  local.get $new_address
  i32.store offset=1
)


(func $_gc_array_contains_pointers
  (param $address i32)
  (result i32)
  local.get $address
  i32.load8_u
  i32.const 0x04
  i32.and
  i32.const 2
  i32.shr_u
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
    i32.const 0x8000
  else
    i32.const 0x10000
  end
  )


(func $_gc_next_stack_base
  (param $current_heap i32)
  (result i32)
  local.get $current_heap
  if (result i32)
    i32.const 0x0004
  else
    i32.const 0x8004
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
  i32.const 2
  i32.shl
)
