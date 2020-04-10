;; Allocates heap space for an object
(func $alloc_object
  (param $total_size_bytes i32)
  (param $size_field i32)
  (param $vtable_pointer i32)

  ;; Returns the address that was allocated
  (result i32)

  (local $allocated_address i32)

  ;; call $gc

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

  ;; Return the allocated address
  local.get $allocated_address
)
(export "alloc_object" (func $alloc_object))


;; Allocates heap space for an array
(func $alloc_array
  (param $size_field i32)

  ;; Returns the address that was allocated
  (result i32)

  (local $allocated_address i32)

  ;; call $gc

  ;; Reserve space for the array
  global.get $heap_last_allocated
  local.get $size_field
  i32.const 5
  i32.add
  i32.sub
  local.tee $allocated_address
  global.set $heap_last_allocated

  ;; Write the flags to the flags field
  ;; 0b00000000
  ;;         ^ has_been_copied GC flag
  ;;          ^ is_object flag
  local.get $allocated_address
  i32.const 0x00
  i32.store8

  ;; Write the size field
  local.get $allocated_address
  local.get $size_field
  i32.store offset=1

  ;; Return the allocated address
  local.get $allocated_address
)
(export "alloc_array" (func $alloc_array))


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


