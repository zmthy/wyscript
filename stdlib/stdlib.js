var $Set, $debug, $newSet, println$VA, str$StA;

str$StA = (function () {
  function keys(o) {
    var i, k;
    if (Object.keys) {
      return Object.keys(o).sort();
    }
    k = [];
    for (i in o) {
      k.push(i);
    }
    return k.sort();
  }
  function join(arr, fn, list) {
    var i, l, r;
    r = [];
    for (i = 0, l = arr.length; i < l; ++i) {
      r.push(fn(arr[i], i));
    }
    return (list ? "[" : "{") + r.join(", ") + (list ? "]" : "}");
  }
  function str(o) {
    var v;
    if (typeof o === 'object') {
      if (o instanceof $Set) {
        return join(o, str);
      } else if (o instanceof Array) {
        return join(o, str, true);
      } else if (o instanceof $Map) {
        return join(keys(o), function (k) {
          return str(k) + "=" + str(o[k]);
        });
      }
      return join(keys(o), function (k) {
        return k + ":" + str(o[k]);
      });
    }
    return o.toString();
  }
  return str;
}());

println$VA = (function () {
  return typeof sysout === 'undefined' ? function () {} : function (o) {
    sysout.println(str$StA(o));
  };
}());

function isLetter$BI(i) {
  return /[a-zA-Z]/.test(String.fromCharCode(i));
}

// Performs a deep clone of a given value.
function $clone(a) {
  var b, i, l;
  b = a;
  if (typeof a === 'object') {
    if (a instanceof Array) {
      b = [];
      for (i = 0, l = a.length; i < l; ++i) {
        b[i] = $clone(a[i]);
      }
    } else {
      b = {};
      for (i in a) {
        b[i] = $clone(a[i]);
      }
    }
  }
  return b;
}

// Ensures two objects are either both not or both of the given types.
function $typecheck(a, b, ts) {
  var at, bt, i, l, t;
  for (i = 0, l = t.length; i < l; ++i) {
    t = ts[i], at = a instanceof t, bt = b instanceof t;
    if (at && !bt || !at && bt) {
      return false;
    }
  }
  return true;
}

// Computes whether two values are strictly equal.
// Recurses down structures to see if all properties are the same.
function $equals(a, b, m) {
  var i, j, k, l, v;
  if (typeof a !== 'object' || typeof b !== 'object') {
    return a === b;
  }
  if (!$typecheck(a, b, [Array, $Map, $Set])) {
    return false;
  }
  try {
    return JSON.stringify(a) === JSON.stringify(b);
  } catch (_) {}
  if (!m) {
    m = $newMap();
  }
  v = m.get(a);
  if (v) {
    if ($indexOf(v, b) > -1) {
      return true;
    }
    v.push(b);
  } else {
    m.put(a, [b]);
  }
  i = j = 0;
  if (a instanceof Array) {
    if (a.length !== b.length) {
      return false;
    }
    for (l = a.length; i < l; ++i) {
      if (!$equals(a[i], b[i], m)) {
        return false;
      }
    }
    return true;
  }
  for (k in a) {
    if (!$equals(a[k], b[k], m)) {
      return false;
    }
    i += 1;
  }
  for (k in b) {
    j += 1;
  }
  if (i !== j) {
    return false;
  }
  return true;
}

// Computes whether an element is in an array.
function $in(a, e) {
  return $indexOf(a, e) > -1;
}

// Computes whether an object has a property.
function $has(r, k) {
  return Object.prototype.hasOwnProperty.call(r, k);
}

// Computes the index of a value in an array.
function $indexOf(a, e) {
  var i, l, p;
  p = Array.prototype.indexOf;
  if (p && (i = p.call(a, e)) > -1) {
    return i;
  }
  for (i = 0, l = a.length; i < l; ++i) {
    if ($equals(e, this[i])) {
      return i;
    }
  }
  return -1;
}

// Intersects two collections into a new collection,
// leaving the originals unaffected.
function $intersect(a, b) {
  var c, i, l, v;
  c = new a.constructor();
  for (i = 0, l = a.length; i < l; ++i) {
    v = a[i];
    if ($in(b, v)) {
      c.push($clone(v));
    }
  }
  return c;
}

// Confirms whether a is a subset of b.
function $subset(a, b, e) {
  var i, l;
  for (i = 0, l = a.length; i < l; ++i) {
    if (!$in(b, a[i])) {
      return false;
    }
  }
  return e ? true : !$equals(a, b);
}

// Logs a debug message to the console.
$debug = (function () {
  var d;
  return typeof console !== 'undefined' &&
        (typeof (d = console.debug) === 'function' ||
         typeof (d = console.log) === 'function') ? function (m) {
    d.call(console, m);
  } : function () {};
}());

// Asserts that a given declaration is strictly true.
function $assert(v) {
  if (v !== true) {
    throw "failed assertion";
  }
}

// Produces a new set collection.
$newSet = (function () {
  var p;
  function Set(a) {
    var i, l;
    for (i = 0, l = a.length; i < l; ++i) {
      this.push(a[i]);
    }
  }
  p = Set.prototype = [];
  p.listPush = p.push;
  p.push = function (v) {
    if (!$in(this, v)) {
      this.listPush(v);
    }
  };
  p.concat = function (a) {
    var c, i, l;
    c = $clone(a);
    for (i = 0, l = a.length; i < l; ++i) {
      c.push(a[i]);
    }
    return c;
  };
  $Set = Set;
  return function () {
    return new Set(arguments);
  };
}());

function $Map() {}

$newMap = (function () {
  return function () {
    var a, i, l, m;
    a = arguments, m = new $Map;
    for (i = 0, l = a.length; i < l; i += 2) {
      m[str$StA(a[i])] = a[i + 1];
    }
    return m;
  }
}());

// // Returns a new map collection.
// $newMap = (function () {
//   var p;
//   function Map(k, v) {
//     this.keys = $clone(k) || [];
//     this.values = $clone(v) || [];
//     this.length = 0;
//   }
//   p = Map.prototype;
//   p.put = function (k, v) {
//     var index;
//     index = $indexOf(this.keys, k);
//     if (index > -1) {
//       this.keys[index] = k;
//       this.values[index] = v;
//     } else {
//       this.keys.push(k);
//       this.values.push(v);
//       this.length += 1;
//     }
//   };
//   p.get = function (k) {
//     var index;
//     index = $indexOf(this.keys, k);
//     if (index > -1) {
//       return this.values[index];
//     }
//     return null;
//   };
//   $Map = Map;
//   return function (k, v) {
//     return new Map(k, v);
//   };
// }());
