create table t_identity_task
(
    id varchar(64) not null,
    processInstanceId varchar(64) not null,
    nodeId varchar(64) not null,
    nodeTaskId varchar(64) not null,
    identityId varchar(64) not null,
    comment text null,
    startTime datetime not null,
    endTime datetime null,
    workFlowState int not null,
    variables varchar(255) not null,
    nextAssignValue varchar(255) null,
    createTime timestamp default CURRENT_TIMESTAMP not null,
    updateTime timestamp default CURRENT_TIMESTAMP not null,
    status smallint default 1 not null,
    constraint t_identity_task_id_uindex
        unique (id)
);

alter table t_identity_task
    add primary key (id);

create table t_model_component
(
    id varchar(64) not null,
    modelId varchar(64) not null,
    componentInfo varchar(512) not null comment '节点/网关/line信息',
    componentType varchar(32) not null comment '组件类型: 节点/网关/line',
    createTime timestamp default CURRENT_TIMESTAMP not null,
    updateTime timestamp default CURRENT_TIMESTAMP not null,
    status smallint default 1 not null,
    constraint t_model_component_id_uindex
        unique (id)
)
    comment '节点/网关/line信息';

alter table t_model_component
    add primary key (id);

create table t_model_config
(
    id varchar(64) not null,
    releaseModel text null,
    tmpModel text null,
    deployTime datetime null,
    createTime datetime null,
    updateTime datetime null,
    status tinyint(1) null,
    isDeploy tinyint(1) default 0 not null,
    name varchar(255) null,
    constraint t_model_config_id_uindex
        unique (id)
);

alter table t_model_config
    add primary key (id);

create table t_node_task
(
    id varchar(64) not null,
    processInstanceId varchar(64) not null,
    curIdentityIds varchar(64) not null,
    identityTaskCnt int not null,
    variables varchar(512) null,
    startTime datetime not null,
    endTime datetime null,
    workFlowState int null,
    doneCnt int null,
    nextAssignValue varchar(255) null,
    nodeId varchar(64) not null,
    createTime timestamp default CURRENT_TIMESTAMP not null,
    updateTime timestamp default CURRENT_TIMESTAMP not null,
    status smallint default 1 not null,
    nodeType varchar(255) ,
    constraint t_node_task_id_uindex
        unique (id)
)
    comment '节点任务';

alter table t_node_task
    add primary key (id);

create table t_process_instance
(
    id varchar(64) not null,
    workFlowState int default 0 not null,
    startTime datetime not null,
    endTime datetime null,
    modelId varchar(64) not null,
    identityId varchar(64) not null,
    variables varchar(512) null,
    createTime timestamp default CURRENT_TIMESTAMP not null,
    updateTime timestamp default CURRENT_TIMESTAMP not null,
    status smallint default 1 not null,
    constraint t_process_instance_id_uindex
        unique (id)
);

alter table t_process_instance
    add primary key (id);

