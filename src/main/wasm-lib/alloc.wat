;; Allocates heap space for an object
(func $alloc_object
  (param $total_size_bytes i32)
  (param $size_field i32)
  (param $vtable_pointer i32)

  ;; Returns the address that was allocated
  (result i32)

  (local $allocated_address i32)

  call $gc

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


;; Allocates heap space for an array
(func $alloc_array
  (param $size_field i32)

  ;; Returns the address that was allocated
  (result i32)

  (local $allocated_address i32)

  call $gc

  ;; Reserve space for the array
  global.get $heap_last_allocated
  local.get $size_field
  i32.const 5
  i32.add
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


;; Resets the memory allocator
(func $reset_allocator
  i32.const 0x8000
  global.set $heap_last_allocated
  i32.const 0x0000
  global.set $stack_base
  i32.const 0
  global.set $stack_offset
  i32.const 0
  global.set $curr_heap
)
(export "reset_allocator" (func $reset_allocator))


;; Pushes a heap address to the shadow stack and returns the
;; allocated index
(func $push_to_stack
  (param $address i32)
  (result i32)
  (local $allocated_offset i32)

  ;; Allocate the next available offset
  global.get $stack_offset
  local.set $allocated_offset

  ;; Save the pushed heap address
  global.get $stack_base
  local.get $allocated_offset
  i32.add
  local.get $address
  i32.store

  ;; Increment the next available offset by 4
  global.get $stack_offset
  i32.const 4
  i32.add
  global.set $stack_offset

  ;; Return the allocated offset
  local.get $allocated_offset
)
