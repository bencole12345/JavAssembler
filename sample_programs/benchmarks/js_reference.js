module.exports.sumSquares = (n) => {
  let sum = 0;
  for (let i = 1; i <= n; i++) {
    sum += i*i;
  }
  return sum;
}

module.exports.recurse = (depth) => {
  if (depth > 0) {
    this.recurse(depth - 1);
  }
}
