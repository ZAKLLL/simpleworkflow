<template>
  <div v-if="easyFlowVisible" style="height: calc(80vh)">
    <el-row>
      <!--顶部工具菜单-->
      <el-col :span="24">
        <div class="ef-tooltar">
          <el-link type="primary" :underline="false">{{ data.name }}</el-link>
          <el-divider direction="vertical"></el-divider>
          <el-button
            type="text"
            icon="el-icon-delete"
            size="large"
            @click="deleteElement"
            :disabled="!this.activeElement.type"
          ></el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button
            type="text"
            icon="el-icon-download"
            size="large"
            @click="downloadData"
          ></el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button
            type="text"
            icon="el-icon-plus"
            size="large"
            @click="zoomAdd"
          ></el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button
            type="text"
            icon="el-icon-minus"
            size="large"
            @click="zoomSub"
          ></el-button>
          <div style="float: right; margin-right: 5px">
            <el-button
              type="info"
              plain
              round
              icon="el-icon-document"
              @click="dataInfo(true)"
              size="mini"
              >流程信息</el-button
            >
            <el-button
              type="info"
              plain
              round
              icon="el-icon-document"
              @click="dataInfo(false)"
              size="mini"
              >流程信息(后端)</el-button
            >

            <el-select
              v-model="modelId"
              placeholder="请选择流程模板"
              @change="dataReloadByModelId"
            >
              <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
              </el-option>
            </el-select>

            <el-select
              v-model="processIntanceId"
              placeholder="请选择流程实例查看实例信息"
              @change="showInstanceHistory"
            >
              <el-option
                v-for="item in processInstanceOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
              </el-option>
            </el-select>

            <el-button
              type="primary"
              plain
              round
              @click="addModelVisible = true"
              icon="el-icon-plus"
              size="mini"
              >新增</el-button
            >

            <el-dialog title="新增模型" :visible.sync="addModelVisible">
              <el-input v-model="newModelName" autocomplete="off"></el-input>
              <div slot="footer" class="dialog-footer">
                <el-button @click="addModelVisible = false">取 消</el-button>
                <el-button type="primary" @click="addNewModel">确 定</el-button>
              </div>
            </el-dialog>

            <el-button
              type="primary"
              plain
              round
              @click="saveModel"
              icon="el-icon-check"
              size="mini"
              >保存</el-button
            >
            <el-button
              type="primary"
              plain
              round
              @click="deploy"
              :disabled="modelId != null && modelId.length > 0 ? false : true"
              icon="el-icon-s-check"
              size="mini"
              >部署</el-button
            >
            <!-- <el-button
              type="primary"
              plain
              round
              @click="dataReloadE"
              icon="el-icon-refresh"
              size="mini"
              >力导图</el-button
            > -->
            <el-button
              type="info"
              plain
              round
              icon="el-icon-document"
              @click="openHelp"
              size="mini"
              >帮助</el-button
            >
          </div>
        </div>
      </el-col>
    </el-row>
    <!--  //画布 -->
    <div style="display: flex; height: calc(100% - 47px)">
      <div style="width: 230px; border-right: 1px solid #dce3e8">
        <node-menu @addNode="addNode" ref="nodeMenu"></node-menu>
      </div>

      <div id="efContainer" ref="efContainer" class="container" v-flowDrag>
        <template v-for="node in data.nodeList">
          <flow-node
            :id="node.id"
            :key="node.id"
            :node="node"
            :activeElement="activeElement"
            @changeNodeSite="changeNodeSite"
            @nodeRightMenu="nodeRightMenu"
            @clickNode="clickNode"
          >
          </flow-node>
        </template>
        <!-- 给画布一个默认的宽度和高度 -->
        <div style="position: absolute; top: 2000px; left: 2000px">&nbsp;</div>
      </div>
      <!-- 右侧表单 -->
      <div
        style="
          width: 300px;
          border-left: 1px solid #dce3e8;
          background-color: #fbfbfb;
        "
      >
        <flow-node-form
          ref="nodeForm"
          @setLineLabel="setLineLabel"
          @repaintEverything="repaintEverything"
        ></flow-node-form>
      </div>
    </div>

    <div style="display: flex; height: calc(30%)">
      <el-table :data="instanceHistory" style="width: 100%" height="250">
        <el-table-column prop="nodeId" label="节点id"> </el-table-column>
        <el-table-column prop="nodeName" label="节点名称"> </el-table-column>
        <el-table-column prop="identityId" label="责任人id"> </el-table-column>
        <el-table-column prop="startTime" label="开始时间"> </el-table-column>
        <el-table-column prop="endTime" label="结束时间"> </el-table-column>
        <el-table-column prop="workFlowState" label="流程状态">
        </el-table-column>
        <el-table-column prop="nextAssignValue" label="后续节点审批人">
        </el-table-column>
        <el-table-column prop="variables" label="审批变量"> </el-table-column>
        <el-table-column prop="comment" label="评论"> </el-table-column>
      </el-table>
    </div>

    <!-- 流程数据详情 -->
    <flow-info v-if="flowInfoVisible" ref="flowInfo" :data="data"></flow-info>
    <flow-help v-if="flowHelpVisible" ref="flowHelp"></flow-help>
  </div>
</template>

<script>
import draggable from "vuedraggable";
// import { jsPlumb } from 'jsplumb'
// 使用修改后的jsplumb
import "./jsplumb";
import { easyFlowMixin } from "@/components/ef/mixins";
import flowNode from "@/components/ef/node";
import nodeMenu from "@/components/ef/node_menu";
import FlowInfo from "@/components/ef/info";
import FlowHelp from "@/components/ef/help";
import FlowNodeForm from "./node_form";
import lodash from "lodash";
import { ForceDirected } from "./force-directed";
import {
  apiGetModels,
  apiSaveModel,
  apiDeploy,
  apiGetModelInstances,
  apiGetInstanceHistory,
} from "./request";
import { genBackEndData } from "./utils";

export default {
  data() {
    return {
      // jsPlumb 实例
      jsPlumb: null,
      // 控制画布销毁
      easyFlowVisible: true,
      // 控制流程数据显示与隐藏
      flowInfoVisible: false,
      // 是否加载完毕标志位
      loadEasyFlowFinish: false,
      flowHelpVisible: false,
      // 数据
      data: {},
      //流程实例历史记录
      instanceHistory: [],

      // 激活的元素、可能是节点、可能是连线
      activeElement: {
        // 可选值 node 、line
        type: undefined,
        // 节点ID
        nodeId: undefined,
        // 连线ID
        sourceId: undefined,
        targetId: undefined,
      },
      zoom: 0.5,

      options: [],
      processInstanceOptions: [],
      processIntanceId: "",
      modelId: "",
      models: {},
      addModelVisible: false,
      newModelName: "",
    };
  },
  // 一些基础配置移动该文件中
  mixins: [easyFlowMixin],
  components: {
    draggable,
    flowNode,
    nodeMenu,
    FlowInfo,
    FlowNodeForm,
    FlowHelp,
  },
  directives: {
    flowDrag: {
      bind(el, binding, vnode, oldNode) {
        if (!binding) {
          return;
        }
        el.onmousedown = (e) => {
          if (e.button == 2) {
            // 右键不管
            return;
          }
          //  鼠标按下，计算当前原始距离可视区的高度
          let disX = e.clientX;
          let disY = e.clientY;
          el.style.cursor = "move";

          document.onmousemove = function (e) {
            // 移动时禁止默认事件
            e.preventDefault();
            const left = e.clientX - disX;
            disX = e.clientX;
            el.scrollLeft += -left;

            const top = e.clientY - disY;
            disY = e.clientY;
            el.scrollTop += -top;
          };

          document.onmouseup = function (e) {
            el.style.cursor = "auto";
            document.onmousemove = null;
            document.onmouseup = null;
          };
        };
      },
    },
  },
  mounted() {
    // axios
    //   .get("www.baidu.com", {

    //   })
    //   .then(function (res) {
    //     console.log("baidu",res)
    //   })
    //   .catch(function (error) {
    //     console.log(error);
    //   });

    this.jsPlumb = jsPlumb.getInstance();
    this.$nextTick(() => {
      // 默认加载models中的第一个流程
      this.getModels().then((models) => {
        // this.dataReload(models[0]);
        this.modelId = models[0].modelId;

        for (var model of models) {
          this.options.push({ value: model.modelId, label: model.name });
          this.models[model.modelId] = model;
        }
        this.dataReloadByModelId();
      });
    });
  },
  methods: {
    // 返回唯一标识
    getUUID() {
      return Math.random().toString(36).substr(3, 10);
    },
    jsPlumbInit() {
      this.jsPlumb.ready(() => {
        // 导入默认配置
        this.jsPlumb.importDefaults(this.jsplumbSetting);
        // 会使整个jsPlumb立即重绘。
        this.jsPlumb.setSuspendDrawing(false, true);
        // 初始化节点
        this.loadEasyFlow();
        // 单点击了连接线, https://www.cnblogs.com/ysx215/p/7615677.html
        this.jsPlumb.bind("click", (conn, originalEvent) => {
          this.activeElement.type = "line";
          this.activeElement.sourceId = conn.sourceId;
          this.activeElement.targetId = conn.targetId;
          this.$refs.nodeForm.lineInit(this.data, {
            from: conn.sourceId,
            to: conn.targetId,
            label: conn.getLabel(),
          });
        });
        // 连线
        this.jsPlumb.bind("connection", (evt) => {
          let from = evt.source.id;
          let to = evt.target.id;
          if (this.loadEasyFlowFinish) {
            this.data.lineList.push({ from: from, to: to, id: this.getUUID() });
          }
        });

        // 删除连线回调
        this.jsPlumb.bind("connectionDetached", (evt) => {
          this.deleteLine(evt.sourceId, evt.targetId);
        });

        // 改变线的连接节点
        this.jsPlumb.bind("connectionMoved", (evt) => {
          this.changeLine(evt.originalSourceId, evt.originalTargetId);
        });

        // 连线右击
        this.jsPlumb.bind("contextmenu", (evt) => {
          console.log("contextmenu", evt);
        });

        // 连线
        this.jsPlumb.bind("beforeDrop", (evt) => {
          let from = evt.sourceId;
          let to = evt.targetId;
          if (from === to) {
            this.$message.error("节点不支持连接自己");
            return false;
          }
          if (this.hasLine(from, to)) {
            this.$message.error("该关系已存在,不允许重复创建");
            return false;
          }
          if (this.hashOppositeLine(from, to)) {
            this.$message.error("不支持两个节点之间连线回环");
            return false;
          }
          if (this.nodeLineCheck(from, to)) {
            return false;
          }
          this.$message.success("连接成功");
          return true;
        });

        // beforeDetach
        this.jsPlumb.bind("beforeDetach", (evt) => {
          console.log("beforeDetach", evt);
        });
        this.jsPlumb.setContainer(this.$refs.efContainer);
      });
    },
    // 加载流程图
    loadEasyFlow() {
      // 初始化节点
      for (var i = 0; i < this.data.nodeList.length; i++) {
        let node = this.data.nodeList[i];
        // 设置源点，可以拖出线连接其他节点
        this.jsPlumb.makeSource(
          node.id,
          lodash.merge(this.jsplumbSourceOptions, {})
        );
        // // 设置目标点，其他源点拖出的线可以连接该节点
        this.jsPlumb.makeTarget(node.id, this.jsplumbTargetOptions);
        if (!node.viewOnly) {
          this.jsPlumb.draggable(node.id, {
            containment: "parent",
            stop: function (el) {
              // 拖拽节点结束后的对调
              console.log("拖拽结束: ", el);
            },
          });
        }
      }
      // 初始化连线
      for (var i = 0; i < this.data.lineList.length; i++) {
        let line = this.data.lineList[i];
        var connParam = {
          source: line.from,
          target: line.to,
          label: line.label ? line.label : "",
          connector: line.connector ? line.connector : "",
          anchors: line.anchors ? line.anchors : undefined,
          paintStyle: line.paintStyle ? line.paintStyle : undefined,
        };
        this.jsPlumb.connect(connParam, this.jsplumbConnectOptions);
      }
      this.$nextTick(function () {
        this.loadEasyFlowFinish = true;
      });
    },
    // 设置连线条件
    setLineLabel(from, to, label, exclusiveOrder) {
      var conn = this.jsPlumb.getConnections({
        source: from,
        target: to,
      })[0];
      if (!label || label === "") {
        conn.removeClass("flowLabel");
        conn.addClass("emptyFlowLabel");
      } else {
        conn.addClass("flowLabel");
      }
      conn.setLabel({
        label: label,
      });
      this.data.lineList.forEach(function (line) {
        if (line.from == from && line.to == to) {
          line.label = label;
          line.exclusiveOrder = exclusiveOrder;
        }
      });
    },
    // 删除激活的元素
    deleteElement() {
      if (this.activeElement.type === "node") {
        this.deleteNode(this.activeElement.nodeId);
      } else if (this.activeElement.type === "line") {
        this.$confirm("确定删除所点击的线吗?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        })
          .then(() => {
            var conn = this.jsPlumb.getConnections({
              source: this.activeElement.sourceId,
              target: this.activeElement.targetId,
            })[0];
            this.jsPlumb.deleteConnection(conn);
          })
          .catch(() => {});
      }
    },
    // 删除线
    deleteLine(from, to) {
      this.data.lineList = this.data.lineList.filter(function (line) {
        if (line.from == from && line.to == to) {
          return false;
        }
        return true;
      });
    },
    // 改变连线
    changeLine(oldFrom, oldTo) {
      this.deleteLine(oldFrom, oldTo);
    },
    // 改变节点的位置
    changeNodeSite(data) {
      for (var i = 0; i < this.data.nodeList.length; i++) {
        let node = this.data.nodeList[i];
        if (node.id === data.nodeId) {
          node.left = data.left;
          node.top = data.top;
        }
      }
    },
    /**
     * 拖拽结束后添加新的节点
     * @param evt
     * @param nodeMenu 被添加的节点对象
     * @param mousePosition 鼠标拖拽结束的坐标
     */
    addNode(evt, nodeMenu, mousePosition) {
      var screenX = evt.originalEvent.clientX,
        screenY = evt.originalEvent.clientY;
      let efContainer = this.$refs.efContainer;
      var containerRect = efContainer.getBoundingClientRect();
      var left = screenX,
        top = screenY;
      // 计算是否拖入到容器中
      if (
        left < containerRect.x ||
        left > containerRect.width + containerRect.x ||
        top < containerRect.y ||
        containerRect.y > containerRect.y + containerRect.height
      ) {
        this.$message.error("请把节点拖入到画布中");
        return;
      }
      left = left - containerRect.x + efContainer.scrollLeft;
      top = top - containerRect.y + efContainer.scrollTop;
      // 居中
      left -= 85;
      top -= 16;
      var nodeId = this.getUUID();
      // 动态生成名字
      var origName = nodeMenu.name;
      var nodeName = origName;
      var index = 1;
      while (index < 10000) {
        var repeat = false;
        for (var i = 0; i < this.data.nodeList.length; i++) {
          let node = this.data.nodeList[i];
          if (node.name === nodeName) {
            nodeName = origName + index;
            repeat = true;
          }
        }
        if (repeat) {
          index++;
          continue;
        }
        break;
      }
      console.log("nodeMenu", nodeMenu);
      var node = {
        id: nodeId,
        name: nodeName,
        type: nodeMenu.type,
        left: left + "px",
        top: top + "px",
        ico: nodeMenu.ico,
        state: nodeMenu.gateway ? "" : "success",
        gateway: nodeMenu.gateway,
      };
      /**
       * 这里可以进行业务判断、是否能够添加该节点
       */
      this.data.nodeList.push(node);
      this.$nextTick(function () {
        this.jsPlumb.makeSource(nodeId, this.jsplumbSourceOptions);
        this.jsPlumb.makeTarget(nodeId, this.jsplumbTargetOptions);
        this.jsPlumb.draggable(nodeId, {
          containment: "parent",
          stop: function (el) {
            // 拖拽节点结束后的对调
            console.log("拖拽结束: ", el);
          },
        });
      });
    },
    /**
     * 删除节点
     * @param nodeId 被删除节点的ID
     */
    deleteNode(nodeId) {
      this.$confirm("确定要删除节点" + nodeId + "?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        closeOnClickModal: false,
      })
        .then(() => {
          /**
           * 这里需要进行业务判断，是否可以删除
           */
          this.data.nodeList = this.data.nodeList.filter(function (node) {
            if (node.id === nodeId) {
              // 伪删除，将节点隐藏，否则会导致位置错位
              // node.show = false
              return false;
            }
            return true;
          });
          this.$nextTick(function () {
            this.jsPlumb.removeAllEndpoints(nodeId);
          });
        })
        .catch(() => {});
      return true;
    },
    clickNode(nodeId) {
      this.activeElement.type = "node";
      this.activeElement.nodeId = nodeId;
      this.$refs.nodeForm.nodeInit(this.data, nodeId);
    },
    // 是否具有该线
    hasLine(from, to) {
      for (var i = 0; i < this.data.lineList.length; i++) {
        var line = this.data.lineList[i];
        if (line.from === from && line.to === to) {
          return true;
        }
      }
      return false;
    },
    // 是否含有相反的线
    hashOppositeLine(from, to) {
      return this.hasLine(to, from);
    },

    nodeLineCheck(from, to) {
      var fromNode = null;
      var lineFromFromNode = null;
      var toNode = null;
      // var lineToToNode = null;
      for (var node of this.data.nodeList) {
        if (node.id == from) {
          fromNode = node;
        } else if (node.id == to) {
          toNode = node;
        }
      }
      for (var line of this.data.lineList) {
        if (line.from == from) {
          lineFromFromNode = line;
        }
      }
      //非网关，已经存在出路
      if (!fromNode.gateway) {
        if (lineFromFromNode != null) {
          this.$message.error("流程节点仅允许有一个出度");
          return true;
        }
        //结束节点不允许有出路
        if (fromNode.type == "END_NODE") {
          this.$message.error("结束节点不允许有出度");
          return true;
        }
      }
      //开始节点不准有入度
      if (toNode.type == "START_NODE") {
        this.$message.error("开始节点不允许有入度");
        return true;
      }

      //开始节点不准直连到网关
      if (fromNode.type == "START_NODE" && toNode.gateway) {
        this.$message.error("开始节点不允许直连到网关");
        return true;
      }

      return false;
    },

    nodeRightMenu(nodeId, evt) {
      this.menu.show = true;
      this.menu.curNodeId = nodeId;
      this.menu.left = evt.x + "px";
      this.menu.top = evt.y + "px";
    },
    repaintEverything() {
      this.jsPlumb.repaint();
    },
    // 流程数据信息
    dataInfo(origin) {
      this.flowInfoVisible = true;
      this.$nextTick(function () {
        this.$refs.flowInfo.init(origin);
      });
    },
    // 加载流程图
    dataReload(data) {
      this.easyFlowVisible = false;
      this.data.nodeList = [];
      this.data.lineList = [];
      this.$nextTick(() => {
        data = lodash.cloneDeep(data);
        this.easyFlowVisible = true;
        this.data = data;
        this.$nextTick(() => {
          this.jsPlumb = jsPlumb.getInstance();
          this.$nextTick(() => {
            this.jsPlumbInit();
          });
        });
      });
    },

    /**
     * 根据下拉的数据加载流程
     */
    dataReloadByModelId() {
      this.dataReload(this.models[this.modelId]);
      //获取当前流程实例数据
      this.getModelInstances(this.modelId)
        .then((res) => {
          this.processInstanceOptions = [];
          this.processIntanceId = null;
          for (var instance of res.data) {
            this.processInstanceOptions.push({
              value: instance.id,
              label: instance.name,
            });
          }
        })
        .catch((err) => {
          console.log("err", err);
          this.$message.error("服务异常");
        });
    },

    zoomAdd() {
      if (this.zoom >= 1) {
        return;
      }
      this.zoom = this.zoom + 0.1;
      this.$refs.efContainer.style.transform = `scale(${this.zoom})`;
      this.jsPlumb.setZoom(this.zoom);
    },
    zoomSub() {
      if (this.zoom <= 0) {
        return;
      }
      this.zoom = this.zoom - 0.1;
      this.$refs.efContainer.style.transform = `scale(${this.zoom})`;
      this.jsPlumb.setZoom(this.zoom);
    },
    // 下载数据
    downloadData() {
      this.$confirm("确定要下载该流程数据吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        closeOnClickModal: false,
      })
        .then(() => {
          var datastr =
            "data:text/json;charset=utf-8," +
            encodeURIComponent(JSON.stringify(this.data, null, "\t"));
          var downloadAnchorNode = document.createElement("a");
          downloadAnchorNode.setAttribute("href", datastr);
          downloadAnchorNode.setAttribute("download", "data.json");
          downloadAnchorNode.click();
          downloadAnchorNode.remove();
          this.$message.success("正在下载中,请稍后...");
        })
        .catch(() => {});
    },
    openHelp() {
      this.flowHelpVisible = true;
      this.$nextTick(function () {
        this.$refs.flowHelp.init();
      });
    },
    //创建新流程模型
    addNewModel() {
      this.addModelVisible = false;
      this.data = {};
      this.data["name"] = this.newModelName;
      this.modelId = null;
      this.dataReload(this.data);
    },

    deploy() {
      apiDeploy(this.modelId)
        .then((res) => {
          var data = res.data;
          if (data.status == "0") {
            this.$message.success("部署成功");
          } else {
            this.$message.error(data.data);
          }
        })
        .catch((err) => {
          this.$message.error("服务异常");
        });
    },
    //保存流程模型到后端并更新下拉框
    saveModel() {
      //deepCopy
      var newModel = JSON.parse(JSON.stringify(this.data));
      if (this.data["modelId"] == undefined) {
        if (this.newModelName.length == 0) {
          this.$message.error("输入正确的名称");
          return;
        }
        newModel["name"] = this.newModelName;
      }
      this.persistModel(newModel)
        .then((newModel) => {
          if (this.data["modelId"] == undefined) {
            this.options.push({
              value: newModel.modelId,
              label: newModel.name,
            });
          }
          this.models[newModel["modelId"]] = newModel;
          this.modelId = newModel.modelId;
          this.data = newModel;
        })
        .catch((err) => {
          this.$message.error("网络异常");
        });
    },

    showInstanceHistory() {
      //todo 禁止拖动
      this.getInstanceHistory(this.processIntanceId)
        .then((res) => {
          for (var instance of res.data) {
            console.log("nodelist:", this.data.nodeList);
            for (var node of this.data.nodeList) {
              if (node.id == instance.nodeId) {
                instance.nodeName = node.name;
              }
            }

            switch (instance.workFlowState) {
              case 0: {
                instance.workFlowState = "未提交";
                break;
              }
              case 1: {
                instance.workFlowState = "审批中";
                break;
              }
              case 2: {
                instance.workFlowState = "已通过";
                break;
              }
              case 3: {
                instance.workFlowState = "已退回";
                break;
              }
              case 9: {
                instance.workFlowState = "关闭";
                break;
              }
            }
          }
          this.instanceHistory = res.data;
        })
        .catch((err) => {
          console.log(err)
          this.$message.error("服务异常",err);
        });
    },

    async getModels() {
      var models = [];
      var res = await apiGetModels();
      if (res.data.status == "0") {
        this.$message.success("查询流程模板成功");
      } else {
        this.$message.error("服务异常");
        return;
      }
      var modelConfigs = res.data.data.records;
      for (var item of modelConfigs) {
        var model = JSON.parse(item.tmpModel);
        model.modelId = item.id;
        models.push(model);
      }
      return models;
    },

    async persistModel(model) {
      var res = await apiSaveModel(genBackEndData(model));
      var data = res.data;
      if (data.status == "0") {
        model["modelId"] = data.data.id;
        this.$message.success("新增/更新成功");
      } else {
        this.$message.error(data.data);
      }
      return model;
    },

    async getModelInstances(modeId) {
      var res = await apiGetModelInstances(modeId);
      var data = res.data;
      if (data.status == "0") {
        this.$message.success("获取流程实例成功");
      } else {
        this.$message.error(data.data);
      }
      return data;
    },

    async getInstanceHistory(processIntanceId) {
      var res = await apiGetInstanceHistory(processIntanceId);
      var data = res.data;
      if (data.status == "0") {
        this.$message.success("获取实例审批记录成功");
      } else {
        this.$message.error(data.data);
      }
      return data;
    },
  },
};
</script>
