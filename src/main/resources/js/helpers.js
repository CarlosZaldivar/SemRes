// http://stackoverflow.com/a/17606289/5459240
String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.split(search).join(replacement);
};