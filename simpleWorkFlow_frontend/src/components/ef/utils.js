// 是否具有该线
export function hasLine(data, from, to) {
    for (let i = 0; i < data.lineList.length; i++) {
        let line = data.lineList[i]
        if (line.from === from && line.to === to) {
            return true
        }
    }
    return false
}

// 是否含有相反的线
export function hashOppositeLine(data, from, to) {
    return hasLine(data, to, from)
}

// 获取连线
export function getConnector(jsp, from, to) {
    let connection = jsp.getConnections({
        source: from,
        target: to
    })[0]
    return connection
}

// 获取唯一标识
export function uuid() {
    return Math.random().toString(36).substr(3, 10)
}


export function genBackEndData(data) {
    var nodeList = data.nodeList;
    var lineList = data.lineList;
    var nodeInLineDict = {};
    var nodeOutLineDict = {};
    var nodes = [];
    var lines = [];
    var gateways = [];

    for (var i = 0; i < lineList.length; i++) {
      var line = lineList[i];
      var fromId = line.from;
      var toId = line.to;
      var lineId = line.id;
      lines.push({
        id: lineId,
        name: line.label,
        pid: fromId,
        sid: toId,
        exclusiveOrder: line.exclusiveOrder,
        flowConditionExpression: line.label,
      });
      if (toId in nodeInLineDict) {
        nodeInLineDict[toId].push(lineId);
      } else {
        nodeInLineDict[toId] = [lineId];
      }
      if (fromId in nodeOutLineDict) {
        nodeOutLineDict[fromId].push(lineId);
      } else {
        nodeOutLineDict[fromId] = [lineId];
      }
    }

    for (var j = 0; j < nodeList.length; j++) {
      var i = nodeList[j];
    //   console.log(i);
      if (i.gateway) {
        var gateway = {
          id: i.id,
          name: i.name,
          type: i.type,
          top: i.top,
          left: i.left,
          ico: i.ico,
          arrivalCnt:i.arrivalCnt,
          pids: nodeInLineDict[i.id],
          sids: nodeOutLineDict[i.id],
        };
        gateways.push(gateway);
      } else {
        var node = {
          id: i.id,
          name: i.name,
          type: i.type,
          top: i.top,
          left: i.left,
          ico: i.ico,
          pid:
            i.id in nodeInLineDict
              ? nodeInLineDict[i.id].length > 0
                ? nodeInLineDict[i.id][0]
                : null
              : null,
          sid:
            i.id in nodeOutLineDict
              ? nodeOutLineDict[i.id].length > 0
                ? nodeOutLineDict[i.id][0]
                : null
              : null,
        };
        nodes.push(node);
      }
    }
    var modelInfo = {
      modelId: data.modelId,
      name:data.name,
      nodes: nodes,
      lines: lines,
      gateways: gateways,
      sourModelInfo: JSON.stringify(data).toString(),
    };
    //   console.log(modelInfo);
    return modelInfo;
  }