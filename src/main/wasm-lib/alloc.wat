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

  ;; Read the current next free heap address
  global.get $next_free_space
  local.set $allocated_address

  ;; Update the next free address
  global.get $next_free_space
  local.get $size
  i32.add
  global.set $next_free_space

  ;; Apply the is_object flag
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
)
(export "reset_allocator" (func $reset_allocator))
