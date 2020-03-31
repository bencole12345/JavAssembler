;; Allocates heap space for an object
(func $alloc_object
  
  ;; The total number of bytes to allocate including header
  ;; and pointer information
  (param $total_size i32)

  ;; The number of bytes occupied by the attributes
  (param $num_attribute_bytes i32)

  ;; Whether to set the is_object flag
  (param $vtable_pointer i32)

  ;; Returns the address that was allocated
  (result i32)

  ;; Used to track the address that was allocated
  (local $allocated_address i32)

  call $gc

  ;; Read the current next free heap address
  global.get $next_free_space
  local.set $allocated_address

  ;; Bump the next free address
  global.get $next_free_space
  local.get $total_size
  i32.add
  global.set $next_free_space

  ;; Write the flags to the first byte
  ;; 0b00000001
  ;;         ^ has_been_copied GC flag
  ;;          ^ is_object flag
  local.get $allocated_address
  i32.const 0x00000001
  i32.store8

  ;; Write the num_attribute_bytes field
  local.get $allocated_address
  local.get $num_attribute_bytes
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
  
  ;; The length of the array
  (param $length i32)

  ;; Returns the address that was allocated
  (result i32)

  ;; Used to track the address that was allocated
  (local $allocated_address i32)

  call $gc

  ;; Read the current next free heap address
  global.get $next_free_space
  local.set $allocated_address

  ;; Size = header + 4 * length
  ;;      = 5      + (length << 2)
  i32.const 5
  local.get $length
  i32.const 2
  i32.shl
  i32.add

  ;; Bump the next free address
  global.get $next_free_space
  i32.add
  global.set $next_free_space

  ;; Write the flags to the first byte
  ;; 0b00000000
  ;;         ^ has_been_copied GC flag
  ;;          ^ is_object flag
  local.get $allocated_address
  i32.const 0x00000000
  i32.store8

  ;; Write the size field
  local.get $allocated_address
  local.get $length
  i32.const 2
  i32.shl
  i32.store offset=1

  ;; Return the allocated address
  local.get $allocated_address
)


;; Resets the memory allocator
(func $reset_allocator
  i32.const 0
  global.set $next_free_space
  i32.const 0
  global.set $shadow_stack_next_offset
  i32.const 32764
  global.set $shadow_stack_base
  i32.const 0
  global.set $gc_curr_heap_half
)
(export "reset_allocator" (func $reset_allocator))


;; Pushes a heap address to the shadow stack and returns the
;; allocated index
(func $push_to_shadow_stack
  (param $heap_address i32)
  (result i32)
  (local $allocated_offset i32)

  ;; Allocate the next available offset
  global.get $shadow_stack_next_offset
  local.set $allocated_offset

  ;; Save the pushed heap address
  global.get $shadow_stack_base
  local.get $allocated_offset
  i32.sub
  local.get $heap_address
  i32.store

  ;; Increment the next available offset by 4
  global.get $shadow_stack_next_offset
  i32.const 4
  i32.add
  global.set $shadow_stack_next_offset

  ;; Return the allocated offset
  local.get $allocated_offset
)
