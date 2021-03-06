var base64EncodeChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
var base64DecodeChars = new Array(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1);

function base64encode(str){
  var out, i, len;
  var c1, c2, c3;
  len = str.length;
  i = 0;
  out = "";
  while (i < len) {
    c1 = str.charCodeAt(i++) & 0xff;
    if (i == len) {
      out += base64EncodeChars.charAt(c1 >> 2);
      out += base64EncodeChars.charAt((c1 & 0x3) << 4);
      out += "==";
      break;
    }
    c2 = str.charCodeAt(i++);
    if (i == len) {
      out += base64EncodeChars.charAt(c1 >> 2);
      out += base64EncodeChars.charAt(((c1 & 0x3) << 4) | ((c2 & 0xF0) >> 4));
      out += base64EncodeChars.charAt((c2 & 0xF) << 2);
      out += "=";
      break;
    }
    c3 = str.charCodeAt(i++);
    out += base64EncodeChars.charAt(c1 >> 2);
    out += base64EncodeChars.charAt(((c1 & 0x3) << 4) | ((c2 & 0xF0) >> 4));
    out += base64EncodeChars.charAt(((c2 & 0xF) << 2) | ((c3 & 0xC0) >> 6));
    out += base64EncodeChars.charAt(c3 & 0x3F);
  }
  return out;
}

function base64decode(str){
  var c1, c2, c3, c4;
  var i, len, out;
  len = str.length;
  i = 0;
  out = "";
  while (i < len) {
    /* c1 */
    do {
      c1 = base64DecodeChars[str.charCodeAt(i++) & 0xff];
    }
    while (i < len && c1 == -1);
    if (c1 == -1)
      break;
    /* c2 */
    do {
      c2 = base64DecodeChars[str.charCodeAt(i++) & 0xff];
    }
    while (i < len && c2 == -1);
    if (c2 == -1)
      break;
    out += String.fromCharCode((c1 << 2) | ((c2 & 0x30) >> 4));
    /* c3 */
    do {
      c3 = str.charCodeAt(i++) & 0xff;
      if (c3 == 61)
        return out;
      c3 = base64DecodeChars[c3];
    }
    while (i < len && c3 == -1);
    if (c3 == -1)
      break;
    out += String.fromCharCode(((c2 & 0XF) << 4) | ((c3 & 0x3C) >> 2));
    /* c4 */
    do {
      c4 = str.charCodeAt(i++) & 0xff;
      if (c4 == 61)
        return out;
      c4 = base64DecodeChars[c4];
    }
    while (i < len && c4 == -1);
    if (c4 == -1)
      break;
    out += String.fromCharCode(((c3 & 0x03) << 6) | c4);
  }
  return out;
}

function parseURL(url){
  parsed_url = {};
  parsed_url.domain = url;

  if ( url == null || url.length == 0 )
    return parsed_url;

  protocol_i = url.indexOf('://');
  parsed_url.protocol = url.substr(0,protocol_i);

  remaining_url = url.substr(protocol_i + 3);
  domain_i = remaining_url.indexOf('/');
  domain_i = domain_i == -1 ? remaining_url.length - 1 : domain_i;
  parsed_url.domain = remaining_url.substr(0, domain_i);
  parsed_url.path = domain_i + 1 == remaining_url.length ? null : remaining_url.substr(domain_i + 1);

  var regex = new RegExp("^(.*\.)?(hunantv.com|funshion.com|kankan.com|pptv.com|cntv.cn|fun.tv|ifeng.com|pps.tv|zjstv.com|iqiyi.com|youku.com|tudou.com|sohu.com|qq.com|m1905.com|1905.com).*$", "gi");
  var match_arr = regex.exec(parsed_url.domain);
  if (match_arr == null) {
    return parsed_url;
  }
  var site = match_arr[2];

  domain_parts = parsed_url.domain.split('.');
  switch ( domain_parts.length ){
    case 2:
      parsed_url.subdomain = null;
      parsed_url.host = domain_parts[0];
      parsed_url.tld = domain_parts[1];
      break;
    case 3:
      parsed_url.subdomain = domain_parts[0];
      parsed_url.host = domain_parts[1];
      parsed_url.tld = domain_parts[2];
      break;
    case 4:
      parsed_url.subdomain = domain_parts[0];
      parsed_url.host = domain_parts[1];
      parsed_url.tld = domain_parts[2] + '.' + domain_parts[3];
      break;
  }
  parsed_url.parent_domain = parsed_url.host + '.' + parsed_url.tld;
  parsed_url.host = site;
  return parsed_url;
}

function fetchSiteInfo(url) {
  parsed_url = parseURL(url);
  return parsed_url.host;
}

function genResult(state, data, message) {
  var result = {};
  result["state"] = state;
  result["message"]  = message;
  str = data.stream;
  if (str) {
    stream = str.replace(/\\\//g, "/");
    result["stream"] = stream;
  } else {
    result["stream"] = "";
  }
  return JSON.stringify(result);
}

function genStream(requestUrl, uStream, apiContent, rules) {
  var message = "GenStream Failed!";
  var data = {'stream':''};
  var html = apiContent;
  var ts = "";
  var te = "";
  if (rules.ts) {
    ts = base64decode(rules.ts);
  }
  if (rules.te) {
    te = base64decode(rules.te);
  }
  if (ts.length == 0 || te.length == 0) {
    message = "No ts and te Found!"
    return genResult("Failed", {}, message);
  }
  r_ts = html.indexOf(ts);
  if (r_ts == -1) {
    message = "No ts Found!"
    return genResult("Failed", {}, message);
  }
  r_te = html.indexOf(te, r_ts + ts.length);
  if (r_te == -1) {
    errmsg = "No te Found!"
    return genResult("Failed", {}, errmsg);
  }
  stream_replace = html.substr(r_ts + ts.length, r_te - r_ts - ts.length);
  var u_stream = uStream;
  var source = fetchSiteInfo(requestUrl);
  if ("iqiyi.com" == source) {
    data["stream"] = stream_replace;
    message = "GenStream Succeed!";
    return genResult("Success", data, message);
  }
  var start_key = "";
  var stop_key = "";
  if ("qq.com" == source) {
    start_key = "vkey=";
    stop_key = "&";
  }
  if ("pptv.com" == source) {
     start_key = "http://";
     stop_key = "/";
  }
  var start_pos = u_stream.indexOf(start_key);
  var stop_pos = u_stream.indexOf(stop_key, start_pos + start_key.length);
  if (start_pos != -1 && stop_pos != -1 && stop_pos > start_pos) {
    stream_address = u_stream.substring(0, start_pos + start_key.length) +
                     stream_replace + u_stream.substring(stop_pos);
    data["stream"] = stream_address;
    message = "GenStream Succeed!";
    return genResult("Success", data, message);
  } 
  return genResult("Failed", data, message);
}

function dealWithRequest(data) {
  var request_data = null;
  try {
    request_data = JSON.parse(data);
  } catch (e) {
    var errmsg = "Parse request JSON Failed!";
    return genResult("Failed", {}, errmsg + " " + e.message);
  }
  var requestUrl = base64decode(request_data["requestUrl"]);
  var apiContent = base64decode(request_data["apiContent"]);
  var uStream = base64decode(request_data["uStream"]);
  var rule = request_data["rule"];
  try {
    return genStream(requestUrl, uStream, apiContent, rule);
  } catch (e) {
    var errmsg = "genStream Failed.";
    return genResult("Failed", {}, errmsg + " " + e.message);
  }
}

function httpGet(theUrl) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open( "GET", theUrl, false );
  xmlHttp.send( null );
  return xmlHttp.responseText;
}
