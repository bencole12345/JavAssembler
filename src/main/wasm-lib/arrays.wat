(func $array_write_i32
  (param $value i32)
  (param $array_address i32)
  (param $index i32)

  (local $size_field i32)
  (local $requested_offset i32)

  ;; Read the array's size field
  local.get $array_address
  i32.load offset=4 align=2
  local.set $size_field

  ;; Determine the offset that has been requested
  local.get $index
  i32.const 2
  i32.shl
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

  ;; It's in the right range so we can safely write the value
  local.get $array_address
  local.get $requested_offset
  i32.add
  local.get $value
  i32.store offset=8 align=2
)


(func $array_read_i32
  (param $array_address i32)
  (param $index i32)
  (result i32)

  (local $size_field i32)
  (local $requested_offset i32)

  ;; Read the array's size field
  local.get $array_address
  i32.load offset=4 align=2
  local.set $size_field

  ;; Determine the offset that has been requested
  local.get $index
  i32.const 2
  i32.shl
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
  i32.load offset=8 align=2
)


(func $array_write_i64
  (param $value i64)
  (param $array_address i32)
  (param $index i32)

  (local $size_field i32)
  (local $requested_offset i32)

  ;; Read the array's size field
  local.get $array_address
  i32.load offset=4 align=2
  local.set $size_field

  ;; Determine the offset that has been requested
  local.get $index
  i32.const 3
  i32.shl
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

  ;; It's in the right range so we can safely write the value
  
  local.get $array_address
  local.get $requested_offset
  i32.add
  local.get $value
  i64.store offset=8 align=2
)


(func $array_read_i64
  (param $array_address i32)
  (param $index i32)
  (result i64)

  (local $size_field i32)
  (local $requested_offset i32)

  ;; Read the array's size field
  local.get $array_address
  i32.load offset=4 align=2
  local.set $size_field

  ;; Determine the offset that has been requested
  local.get $index
  i32.const 3
  i32.shl
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
  i64.load offset=8 align=2
)


(func $array_write_f32
  (param $value f32)
  (param $array_address i32)
  (param $index i32)

  (local $size_field i32)
  (local $requested_offset i32)

  ;; Read the array's size field
  local.get $array_address
  i32.load offset=4 align=2
  local.set $size_field

  ;; Determine the offset that has been requested
  local.get $index
  i32.const 2
  i32.shl
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

  ;; It's in the right range so we can safely write the value
  
  local.get $array_address
  local.get $requested_offset
  i32.add
  local.get $value
  f32.store offset=8 align=2
)


(func $array_read_f32
  (param $array_address i32)
  (param $index i32)
  (result f32)

  (local $size_field i32)
  (local $requested_offset i32)

  ;; Read the array's size field
  local.get $array_address
  i32.load offset=4 align=2
  local.set $size_field

  ;; Determine the offset that has been requested
  local.get $index
  i32.const 2
  i32.shl
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
  f32.load offset=8 align=2
)


(func $array_write_f64
  (param $value f64)
  (param $array_address i32)
  (param $index i32)

  (local $size_field i32)
  (local $requested_offset i32)

  ;; Read the array's size field
  local.get $array_address
  i32.load offset=4 align=2
  local.set $size_field

  ;; Determine the offset that has been requested
  local.get $index
  i32.const 3
  i32.shl
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

  ;; It's in the right range so we can safely write the value
  
  local.get $array_address
  local.get $requested_offset
  i32.add
  local.get $value
  f64.store offset=8 align=2
)


(func $array_read_f64
  (param $array_address i32)
  (param $index i32)
  (result f64)

  (local $size_field i32)
  (local $requested_offset i32)

  ;; Read the array's size field
  local.get $array_address
  i32.load offset=4 align=2
  local.set $size_field

  ;; Determine the offset that has been requested
  local.get $index
  i32.const 3
  i32.shl
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
  f64.load offset=8 align=2
)
