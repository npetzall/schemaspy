function $(expr, con) {
  return typeof expr === 'string'? (con || document).querySelector(expr) : expr;
}

function $$(expr, con) {
  return Array.prototype.slice.call((con || document).querySelectorAll(expr));
}

function xhr(o) {
  var xhr = new XMLHttpRequest(o.src);

  xhr.open("GET", o.src);

  xhr.onreadystatechange = function () {
    if (xhr.readyState == 4) {
      if (xhr.status < 400) {
        try {
          o.onsuccess.call(xhr);
        }
        catch (e) {
          o.onerror.call(xhr, e);
        }
      }
      else {
        o.onerror.call(xhr);
      }
    }
  };

  xhr.send();
}

(function(){

  xhr({
    src: 'latest.json',
    onsuccess: function () {
      var latest = JSON.parse(this.responseText);
      if (latest.filename) {
        $( 'section.lookup > h1').innerHTML = 'Latest development build ' + latest.version;
        $('section.lookup > p').innerHTML = 'Downloading <a href="' + latest.filename + '">' + latest.filename + '</a>';
        location.href = latest.filename;
      }
      else {
        $('section.lookup > p').innerHTML = 'Lookup failed, no information about latest development build…';
        $('section.lookup > p').className =  "warning";
      }
    },
    onerror: function (xhr, e) {
      if (e) {
        console.log(e);
      } else {
        $('section.lookup > p').innerHTML = 'Lookup failed, no information about latest development build…';
        $('section.lookup > p').className =  "warning";
      }
    }
  });

})();