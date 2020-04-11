;; Allocates heap space for an object
(func $alloc_object
  (param $total_size_bytes i32)
  (param $size_field i32)
  (param $vtable_pointer i32)

  ;; Returns the address that was allocated
  (result i32)

  (local $allocated_address i32)

  ;; If needed, run the garbage collector
  local.get $total_size_bytes
  call $determine_gc_needed
  if
    call $gc
  end

  ;; Reserve space for the object
  global.get $heap_last_allocated
  local.get $total_size_bytes
  i32.sub
  local.tee $allocated_address
  global.set $heap_last_allocated

  ;; Write the flags to the flags field
  ;; 0b00000001
  ;;         ^ has_been_copied GC flag
  ;;          ^ is_object flag
  local.get $allocated_address
  i32.const 0x01
  i32.store8

  ;; Write the size field
  local.get $allocated_address
  local.get $size_field
  i32.store offset=1

  ;; Write the vtable pointer
  local.get $allocated_address
  local.get $vtable_pointer
  i32.store offset=5

  ;; Write zeroes to every attribute
  local.get $allocated_address
  i32.const 9
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
  ;; TODO: Pass in the bit for whether the array contains pointers
  ;; Returns the address that was allocated
  (result i32)
  (local $allocated_address i32)

  ;; If needed, run the garbage collector
  local.get $size_field
  i32.const 5
  i32.add
  call $determine_gc_needed
  if
    call $gc
  end

  ;; Reserve space for the array
  global.get $heap_last_allocated
  local.get $size_field
  i32.const 5
  i32.add
  i32.sub
  local.tee $allocated_address
  global.set $heap_last_allocated

  ;; Write the flags to the flags field
  ;; 0b00000100
  ;;        ^ contains_pointers
  ;;         ^ has_been_copied GC flag
  ;;          ^ is_object flag
  local.get $allocated_address
  i32.const 0x04
  i32.store8

  ;; Write the size field
  local.get $allocated_address
  local.get $size_field
  i32.store offset=1

  ;; Write zeroes to every element
  local.get $allocated_address
  i32.const 5
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

  (block (loop
    local.get $pos
    local.get $end
    i32.ge_u
    br_if 1

    local.get $pos
    i32.const 0
    i32.store

    local.get $pos
    i32.const 4
    i32.add
    local.set $pos
    br 0
  ))
)


(func $determine_gc_needed
  (param $requested_amount i32)
  (result i32)

  ;; Amount the caller has requested
  local.get $requested_amount

  ;; Add 1024 bytes extra just to be safe
  i32.const 1024
  i32.add

  ;; Amount of free space right now
  global.get $heap_last_allocated
  global.get $stack_base
  global.get $stack_pointer
  i32.add
  i32.sub

  ;; If it's not enough then we need to run the garbage collector
  i32.ge_u
)


;; Resets the memory allocator
(func $reset_allocator
  i32.const 0x8000
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


(func $write_to_array_index
  (param $value i32)
  (param $array_address i32)
  (param $index i32)
  (param $element_size i32)

  ;; TODO: Add bounds checking
  
  local.get $array_address
  local.get $index
  local.get $element_size
  i32.mul
  i32.add
  local.get $value
  i32.store offset=5
)


(func $read_array_index
  (param $array_address i32)
  (param $index i32)
  (param $element_size i32)
  (result i32)

  (local $size_field i32)
  (local $requested_offset i32)

  ;; Read the array's size field
  local.get $array_address
  i32.load offset=1
  local.set $size_field

  ;; Determine the offset that has been requested
  local.get $index
  local.get $element_size
  i32.mul
  local.tee $requested_offset

  ;; Check it's >= 0
  i32.const 0
  i32.lt_s
  if
    ;; Trap
    unreachable
  end

  ;; Check it's <= size_field
  local.get $requested_offset
  local.get $size_field
  i32.ge_s
  if
    ;; Trap
    unreachable
  end

  ;; It's in the right range so we can safely look up the index
  local.get $array_address
  local.get $requested_offset
  i32.add
  i32.load offset=5
)
