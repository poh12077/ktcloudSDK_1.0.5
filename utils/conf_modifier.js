const fs = require('fs')

let exec = require('child_process').exec;
let child;

child = exec("hostname", function (error, hostname, stderr) {
  exec("hostname -I", function (error, ip, stderr) {
    let data = {
      hostname: "",
      ip: ""
    }
    data.hostname = hostname.replace(/\n|\r|\s*/g, "");
    data.ip = ip.replace(/\n|\r|\s*/g, "");
    read(data);

    if (error !== null) {
      console.log('exec error: ' + error);
    }
  });
  if (error !== null) {
    console.log('exec error: ' + error);
  }
});


let read = (data) => {
  fs.readFile('/usr/service/etc/solrtmp/server.json', 'utf8', (err, conf) => {
    if (err) {
      console.error(err)
      return
    }
    parser_server(conf, data);
  })

  fs.readFile('/usr/service/etc/stfw/stfw.json', 'utf8', (err, conf) => {
    if (err) {
      console.error(err)
      return
    }
    parser_stfw(conf, data);
  })
}

let parser_server = (conf, data) => {
  conf = JSON.parse(conf);
  conf.server.id = data.hostname;
  conf.server.host = data.ip;
  conf.manager_server.url = "test";
  conf = JSON.stringify(conf, null, "\t");

  fs.writeFile('/usr/service/etc/solrtmp/server.json', conf, function (err) {
    if (err) throw err;
    console.log('server.json has been modified');
  });
}

let parser_stfw = (conf, data) => {
  conf = JSON.parse(conf);
  conf.id = data.hostname;
  conf = JSON.stringify(conf, null, "\t");

  fs.writeFile('/usr/service/etc/stfw/stfw.json', conf, function (err) {
    if (err) throw err;
    console.log('stfw.json has been modified');
  });
}



