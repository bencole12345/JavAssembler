;; The memory used for the heap
;; Here the 1 means we need at least one 1 page (64 KiB)
(memory (export "memory") 1)

;; Used to track the next available heap address
(global $next_free_space (mut i32) (i32.const 0))

;; Used as temporary values when allocating objects
(global $temp_ref_heap_address (mut i32) (i32.const 0))
(global $temp_ref_shadow_stack_offset (mut i32) (i32.const 0))

;; Pointer to the top of the shadow stack (grows downwards)
(global $shadow_stack_base (mut i32) (i32.const 65532)
)
(global $shadow_stack_next_offset (mut i32) i32.const 0)
