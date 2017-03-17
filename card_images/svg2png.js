svg2png = require("gulp-svg2png");

source = process.argv[2];
dest = process.argv[3];

console.log("Fuck you world");
console.log(source);
console.log(dest);

svg2png(source, dest, function (err) {
      if(err) {
              consle.log("svg conversion fail: "+source);
                }
});
