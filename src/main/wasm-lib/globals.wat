;; The memory used for the heap
;; Here the 1 means we need at least one 1 page (64 KiB)
(memory (export "memory") 1)

;; Used to track the next available heap address (grows downwards)
(global $heap_last_allocated (mut i32) (i32.const 0x8000))

;; Pointer to the stack base (grows upwards)
(global $stack_base (mut i32) (i32.const 0x0000))
(global $stack_offset (mut i32) i32.const 0)

;; Used as temporary values when allocating objects
(global $temp_heap_address (mut i32) (i32.const 0))
(global $temp_stack_offset (mut i32) (i32.const 0))


;; Which half of the heap we are currently using (0 or 1)
(global $curr_heap (mut i32) (i32.const 0))
