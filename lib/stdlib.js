var $debug, $newMap, $newSet;

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

// Computes whether two values are strictly equal.
// Recurses down structures to see if all properties are the same.
function $equals(a, b, m) {
  var i, j, k, l, v;
  if (typeof a !== 'object' || typeof b !== 'object') {
    return a === b;
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
    if (!(b instanceof Array) || a.length !== b.length) {
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

function $subset(a, b, e) {
  var i, l;
  for (i = 0, l = a.length; i < l; ++i) {
    if (!$in(b, a[i])) {
      return false;
    }
  }
  return e ? true : !$equals(a, b);
}

$debug = (function () {
  var d;
  return typeof console !== 'undefined' &&
        (typeof (d = console.debug) === 'function' ||
         typeof (d = console.log) === 'function' ? function (m) {
    d(m);
  } : function () {};
}());

function $assert(v) {
  if (v !== true) {
    throw "failed assertion";
  }
}

$newSet = (function () {
  function Set(a) {
    var i, l;
    for (i = 0, l = a.length; i < l; ++i) {
      this.push(a[i]);
    }
  }
  Set.prototype.push = function (v) {
    if (!$in(this.values, v)) {
      this.values.push(v);
      this.length += 1;
    }
  }
  Set.prototype.concat = function (a) {
    var c, i, l;
    c = $clone(a);
    for (i = 0, l = a.length; i < l; ++i) {
      c.push(a[i]);
    }
    return c;
  }
  return function () {
    return new Set(arguments);
  };
}());

// Returns a new Map.
$newMap = (function () {
  function Map(k, v) {
    this.keys = k || [];
    this.values = v || [];
  }
  Map.prototype.put = function (k, v) {
    var index;
    index = $indexOf(this.keys, k);
    if (index > -1) {
      this.keys[index] = k;
      this.values[index] = v;
    }
    this.keys.push(k);
    this.values.push(v);
  };
  Map.prototype.get = function (k) {
    var index;
    index = $indexOf(this.keys, k);
    if (index > -1) {
      return this.values[index];
    }
    return null;
  };
  Map.prototype.remove = function (k) {
    var index;
    index = $indexOf(this.keys, k);
    if (index > -1) {
      this.keys.splice(index, 1);
      this.values.splice(index, 1);
    }
  };
  return function (k, v) {
    return new Map(k, v);
  };
}());
