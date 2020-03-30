;; Allocates a requested amount of heap space and returns
;; the address that was allocated.
(func $alloc
  
  ;; The number of bytes to allocate
  (param $size i32)

  ;; Whether to set the is_object flag
  (param $is_object i32)

  ;; Returns the address that was allocated
  (result i32)

  ;; Used to track the address that was allocated
  (local $allocated_address i32)

  ;; Used to track the shadow stack offset allocated
  (local $allocated_shadow_stack_offset i32)

  ;; Read the current next free heap address
  global.get $next_free_space
  local.set $allocated_address

  ;; Bump the next free address
  global.get $next_free_space
  local.get $size
  i32.add
  global.set $next_free_space

  ;; Set the is_object flag
  local.get $is_object
  i32.const 31
  i32.shl
  local.get $size
  i32.or
  local.set $size

  ;; Write the size word to the first address
  local.get $allocated_address
  local.get $size
  i32.store

  ;; Return the allocated address
  local.get $allocated_address
)


;; Resets the memory allocator
(func $reset_allocator
  i32.const 0
  global.set $next_free_space
  i32.const 0
  global.set $shadow_stack_next_offset
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
