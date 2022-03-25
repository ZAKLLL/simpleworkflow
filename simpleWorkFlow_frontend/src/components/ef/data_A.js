let dataA = {
    name: '流程A',
    modelId:"modelA",
    nodeList: [
        {
            id: 'nodeA',
            name: '流程A-节点A',
            type: 'START_NODE',
            left: '26px',
            top: '161px',
            ico: 'el-icon-user-solid'
        },
        {
            id: 'nodeB',
            name: '流程A-节点B',
            type: 'SINGLE_USER_TASK_NODE',
            left: '340px',
            top: '161px',
            ico: 'el-icon-goods'
        },
        {
            id: 'nodeC',
            name: '流程A-节点C',
            type: 'END_NODE',
            left: '739px',
            top: '161px',
            ico: 'el-icon-present'
        }
    ],
    lineList: [{
        from: 'nodeA',
        to: 'nodeB',
        id:"ida",
        exclusiveOrder: 0,
    }, {
        from: 'nodeB',
        to: 'nodeC',
        id:"idb",
        exclusiveOrder: 0,
    }]
}

export function getDataA () {
    return dataA
}
