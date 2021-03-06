(func $alloc
  (param $size_bytes i32)
  (result i32)

  ;; If there's not enough free space then run the garbage collector
  local.get $size_bytes
  call $not_enough_free_space
  if
    call $gc
  end
  
  ;; If there's still not enough free space then repeatedly request
  ;; more until there is
  block
    loop
      local.get $size_bytes
      call $not_enough_free_space
      i32.const 1
      i32.xor
      br_if 1
      call $request_more_memory
      br 0
    end
  end

  ;; Allocate the space
  global.get $heap_last_allocated
  local.get $size_bytes
  i32.sub
  global.set $heap_last_allocated

  ;; Return the allocated address
  global.get $heap_last_allocated
)

;; Allocates heap space for an object
(func $alloc_object
  (param $total_size_bytes i32)
  (param $size_field i32)
  (param $vtable_pointer i32)

  ;; Returns the address that was allocated
  (result i32)

  (local $allocated_address i32)

  local.get $total_size_bytes
  call $alloc
  local.tee $allocated_address

  ;; Write the flags to the flags field
  ;; 0b00000001
  ;;         ^ has_been_copied GC flag
  ;;          ^ is_object flag
  i32.const 0x00000001
  i32.store

  ;; Write the size field
  local.get $allocated_address
  local.get $size_field
  i32.store offset=4 align=2

  ;; Write the vtable pointer
  local.get $allocated_address
  local.get $vtable_pointer
  i32.store offset=8 align=2

  ;; Write zeroes to every attribute
  local.get $allocated_address
  i32.const 12
  i32.add
  local.get $size_field
  call $write_zeroes

  ;; Return the allocated address
  local.get $allocated_address
)
(export "alloc_object" (func $alloc_object))


;; Allocates heap space for an array
(func $alloc_array
  (param $size_field i32)
  (param $contains_pointers i32)
  (result i32)

  (local $allocated_address i32)

  ;; Reserve space for the array
  local.get $size_field
  i32.const 8
  i32.add
  call $alloc
  local.tee $allocated_address

  ;; Write the flags to the flags field
  local.get $contains_pointers
  i32.const 2
  i32.shl
  i32.store

  ;; Write the size field
  local.get $allocated_address
  local.get $size_field
  i32.store offset=4 align=2

  ;; Write zeroes to every element
  local.get $allocated_address
  i32.const 8
  i32.add
  local.get $size_field
  call $write_zeroes

  ;; Return the allocated address
  local.get $allocated_address
)
(export "alloc_array" (func $alloc_array))


(func $write_zeroes
  (param $address i32)
  (param $length i32)
  (local $pos i32)
  (local $end i32)

  local.get $address
  local.get $length
  i32.add
  local.set $end

  local.get $address
  local.set $pos

  block
    loop
      local.get $pos
      local.get $end
      i32.ge_u
      br_if 1

      local.get $pos
      i32.const 0
      i32.store align=2

      local.get $pos
      i32.const 4
      i32.add
      local.set $pos
      br 0
    end
  end
)


(func $not_enough_free_space
  (param $requested_amount i32)
  (result i32)
  local.get $requested_amount
  i32.const 32
  i32.add
  global.get $heap_last_allocated
  global.get $stack_base
  global.get $stack_pointer
  i32.add
  i32.sub
  i32.gt_u
)


;; Resets the memory allocator
(func $reset_allocator
  global.get $memory_pages
  i32.const 15
  i32.shl
  global.set $heap_last_allocated
  i32.const 0x0000
  global.set $stack_base
  i32.const 0
  global.set $stack_frame_start
  i32.const 0
  global.set $stack_pointer
  i32.const 0
  global.set $curr_heap
)
(export "reset_allocator" (func $reset_allocator))


(func $set_at_stack_frame_offset
  (param $value i32)
  (param $offset i32)

  (local $relative_to_stack_base i32)

  ;; Compute the address relative to stack base
  global.get $stack_frame_start
  local.get $offset
  i32.add
  local.tee $relative_to_stack_base

  ;; Set the value
  global.get $stack_base
  i32.add
  local.get $value
  i32.store

  ;; If this is the highest offset we've seen so far, update stack pointer
  local.get $relative_to_stack_base
  global.get $stack_pointer
  i32.ge_u
  if
    local.get $relative_to_stack_base
    i32.const 4
    i32.add
    global.set $stack_pointer
  end
)


(func $request_more_memory
  (local $curr_word i32)
  (local $curr_heap_object i32)
  (local $delta i32)
  (local $heap_end i32)
  (local $heap_to_address i32)
  (local $size_field i32)
  (local $array_contains_pointers i32)
  (local $pointer_info_pos i32)
  (local $num_pointer_info_words i32)
  (local $object_end i32)
  (local $value i32)
  
  ;; Request more memory
  global.get $memory_pages
  memory.grow
  drop

  ;; Record the new size
  global.get $memory_pages
  i32.const 1
  i32.shl
  global.set $memory_pages

  ;; Work out how much stuff is going to be shifted by
  global.get $memory_pages
  i32.const 14
  i32.shl
  local.set $delta

  ;; Determine which half of the memory is currently active
  global.get $curr_heap
  if
    ;; 1 - We are currently using the second half
    ;;  => Need to move the stack

    ;; Copy across each word
    i32.const 0
    local.set $curr_word
    block
      loop
        local.get $curr_word
        ;; Check we're not off by 4 here - what is the stack pointer relative to?
        global.get $stack_pointer
        i32.ge_u
        br_if 1

        ;; Address to write to
        i32.const 4
        local.get $curr_word
        i32.add

        ;; Value to write
        global.get $stack_base
        local.get $curr_word
        i32.add
        i32.load align=2

        ;; Write it
        i32.store align=2

        local.get $curr_word
        i32.const 4
        i32.add
        local.set $curr_word
        br 0
      end
    end

    ;; Update new stack base
    i32.const 0x0004
    global.set $stack_base

  else
    ;; 0 - We are currently using the first half
    ;;  => Need to move the heap

    global.get $heap_last_allocated
    local.set $curr_heap_object

    local.get $delta
    local.set $heap_end

    ;; Copy over each object
    block
      loop
        local.get $curr_heap_object
        local.get $heap_end
        i32.ge_u
        br_if 1

        ;; Work out the new destination
        local.get $curr_heap_object
        local.get $delta
        i32.add
        local.set $heap_to_address

        ;; Copy across the header
        local.get $heap_to_address
        local.get $curr_heap_object
        i32.load align=2
        i32.store align=2

        ;; Copy across the size field
        local.get $heap_to_address
        local.get $curr_heap_object
        i32.load offset=4 align=2
        local.tee $size_field
        i32.store offset=4 align=2

        ;; Determine whether it's an array or an object
        local.get $curr_heap_object
        call $_gc_is_object
        if
          ;; Object
          
          ;; Copy across the vtable pointer
          local.get $heap_to_address
          local.get $curr_heap_object
          i32.load offset=8 align=2
          i32.store offset=8 align=2

          ;; Move across each attribute
          i32.const 0
          local.set $curr_word
          block
            loop
              local.get $curr_word
              local.get $size_field
              i32.ge_u
              br_if 1

              ;; Address to write to
              local.get $heap_to_address
              local.get $curr_word
              i32.add

              ;; Value to write
              local.get $curr_heap_object
              local.get $curr_word
              call $_gc_is_pointer
              if (result i32)
                local.get $curr_heap_object
                local.get $curr_word
                i32.add
                i32.load offset=12 align=2
                local.tee $value
                i32.eqz
                if (result i32)
                  local.get $value
                else
                  local.get $value
                  local.get $delta
                  i32.add
                end
              else
                local.get $curr_heap_object
                local.get $curr_word
                i32.add
                i32.load offset=12 align=2
              end

              ;; Write it
              i32.store offset=12 align=2

              ;; Move to next word
              local.get $curr_word
              i32.const 4
              i32.add
              local.set $curr_word
            end
          end

          ;; Copy pointer info bits

          ;; Work out where the pointer info region starts
          local.get $curr_heap_object
          i32.const 12
          i32.add
          local.get $size_field
          i32.add
          local.set $pointer_info_pos

          ;; Work out where the pointer info region ends
          local.get $pointer_info_pos
          local.get $size_field
          call $num_bytes_to_pointer_info_length
          i32.add
          local.set $object_end

          ;; Copy the pointer info, one word at a time
          block
            loop
              local.get $pointer_info_pos
              local.get $object_end
              i32.ge_u
              br_if 1

              ;; Address to write to
              local.get $pointer_info_pos
              local.get $delta
              i32.add

              ;; Value to write
              local.get $pointer_info_pos
              i32.load align=2

              ;; Write it
              i32.store align=2

              local.get $pointer_info_pos
              i32.const 4
              i32.add
              local.set $pointer_info_pos
              br 0
            end
          end

        else
          ;; Array

          local.get $curr_heap_object
          call $_gc_array_contains_pointers
          local.set $array_contains_pointers

          i32.const 0
          local.set $curr_word
          block
            loop
              local.get $curr_word
              local.get $size_field
              i32.ge_u
              br_if 1

              ;; Address to write to
              local.get $curr_heap_object
              local.get $curr_word
              i32.add
              local.get $delta
              i32.add

              ;; Value to write
              local.get $array_contains_pointers
              if (result i32)
                ;; Need to adjust the pointer
                local.get $curr_heap_object
                local.get $curr_word
                i32.add
                i32.load offset=8 align=2
                local.get $delta
                i32.add
              else
                ;; It's primitive so don't change it
                local.get $curr_heap_object
                local.get $curr_word
                i32.add
                i32.load offset=8 align=2
              end

              ;; Write it
              i32.store offset=8 align=2

              local.get $curr_word
              i32.const 4
              i32.add
              local.set $curr_word
              br 0
            end
          end
        end

        ;; Move to the next heap object and restart
        local.get $curr_heap_object
        local.get $curr_heap_object
        call $_gc_determine_size
        i32.add
        local.set $curr_heap_object
        br 0
      end
    end

    global.get $heap_last_allocated
    local.get $delta
    i32.add
    global.set $heap_last_allocated

    ;; Correct every pointer in the stack
    i32.const 0
    local.set $curr_word
    block
      loop
        local.get $curr_word
        global.get $stack_pointer
        i32.ge_u
        br_if 1

        ;; Address to write to
        i32.const 4
        local.get $curr_word
        i32.add

        ;; Value to write
        global.get $stack_base
        local.get $curr_word
        i32.add
        i32.load align=2
        local.get $delta
        i32.add

        ;; Write it
        i32.store align=2

        local.get $curr_word
        i32.const 4
        i32.add
        local.set $curr_word
        br 0
      end
    end
  end

  ;; In all cases we are now using heap 0
  i32.const 0
  global.set $curr_heap
)
