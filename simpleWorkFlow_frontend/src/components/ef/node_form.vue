<template>
  <div>
    <div class="ef-node-form">
      <div class="ef-node-form-header">编辑</div>
      <div class="ef-node-form-body">
        <el-form
          :model="node"
          ref="dataForm"
          label-width="80px"
          v-show="type === 'node'"
        >
          <el-form-item label="nodeId">
            <el-input v-model="node.id" :disabled="true"></el-input>
          </el-form-item>
          <el-form-item label="类型">
            <el-input v-model="node.type" :disabled="true"></el-input>
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model="node.name"></el-input>
          </el-form-item>

          <el-form-item
            label="多人会签比例"
            v-show="node.type === 'MULTI_USER_TASK_NODE'"
          >
            <el-input-number
              :min="0"
              :controls="false"
              :precision="2"
              v-model="node.mutliCompleteRatio"
            ></el-input-number>
          </el-form-item>

          <el-form-item
            label="任务执行器"
            v-show="node.type === 'EVENT_TASK_NODE'"
          >
            <el-input v-model="node.eventTaskExecutor"></el-input>
          </el-form-item>

          <el-form-item label="left坐标">
            <el-input v-model="node.left" :disabled="true"></el-input>
          </el-form-item>
          <el-form-item label="top坐标">
            <el-input v-model="node.top" :disabled="true"></el-input>
          </el-form-item>
          <el-form-item label="ico图标">
            <el-input v-model="node.ico"></el-input>
          </el-form-item>

          <el-form-item label="状态">
            <el-select
              v-model="node.state"
              placeholder="请选择"
              :disabled="true"
            >
              <el-option
                v-for="item in stateList"
                :key="item.state"
                :label="item.label"
                :value="item.state"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button icon="el-icon-close" @click="nodeReset">重置</el-button>
            <el-button type="primary" icon="el-icon-check" @click="save"
              >保存</el-button
            >
          </el-form-item>
        </el-form>

        <el-form
          :model="line"
          ref="dataForm"
          label-width="80px"
          v-show="showLineForm"
        >
          <el-form-item label="条件">
            <el-input v-model="line.label"></el-input>
          </el-form-item>
          <el-form-item label="优先级">
            <el-input-number
              :min="0"
              :controls="false"
              :precision="0"
              v-model="line.exclusiveOrder"
            ></el-input-number>
          </el-form-item>

          <el-form-item>
            <el-button icon="el-icon-close" @click="lineReset">重置</el-button>
            <el-button type="primary" icon="el-icon-check" @click="saveLine"
              >保存</el-button
            >
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { cloneDeep } from "lodash";
export default {
  data() {
    return {
      visible: true,
      // node 或 line
      type: "node",
      node: {},
      line: {},
      data: {},
      showLineForm: false,
      stateList: [
        {
          state: "success",
          label: "成功",
        },
        {
          state: "closed",
          label: "关闭",
        },
        {
          state: "recalled",
          label: "撤回",
        },
        {
          state: "error",
          label: "错误",
        },
        {
          state: "running",
          label: "运行中",
        },
      ],
    };
  },
  methods: {
    /**
     * 表单修改，这里可以根据传入的ID进行业务信息获取
     * @param data
     * @param id
     */
    nodeInit(data, id) {
      this.type = "node";
      this.data = data;
      data.nodeList.filter((node) => {
        if (node.id === id) {
          this.node = cloneDeep(node);
        }
      });
    },
    lineInit(data, line) {
      this.type = "line";
      this.line = line;
      this.data = data;
      data.lineList.filter((i) => {
        if (i.from === line.from && i.to === line.to) {
          this.line["exclusiveOrder"] = i["exclusiveOrder"];
        }
      });
      this.showLineForm = this.ifShowLineForm();
    },
    // 修改连线
    saveLine() {
      this.$emit(
        "setLineLabel",
        this.line.from,
        this.line.to,
        this.line.label,
        this.line.exclusiveOrder
      );
    },
    save() {
      this.data.nodeList.filter((node) => {
        if (node.id === this.node.id) {
          node.name = this.node.name;
          node.left = this.node.left;
          node.top = this.node.top;
          node.ico = this.node.ico;
          node.state = this.node.state;
          node.eventTaskExecutor = this.node.eventTaskExecutor;
          node.mutliCompleteRatio = this.node.mutliCompleteRatio;
          this.$emit("repaintEverything");
        }
      });
      //   this.$message.succss("保存成功")
    },
    nodeReset() {
      this.data.nodeList.filter((node) => {
        if (node.id === this.node.id) {
          this.node = cloneDeep(node);
        }
      });
      //   this.$message.succss("重置成功")
    },
    lineReset() {
      this.data.lineList.filter((line) => {
        if (line.from == this.line.from && line.to == this.line.to) {
          this.line = cloneDeep(line);
        }
      });
      //   this.$message.succss("重置成功")
    },
    ifShowLineForm() {
      if (this.type != "line") {
        return false;
      }
      //line 的 from节点必须是 EXCLUSIVE_GATEWAY
      for (var node of this.data.nodeList) {
        if (this.line.from == node.id) {
          return node.type == "EXCLUSIVE_GATEWAY";
        }
      }
      return false;
    },
  },
};
</script>

<style>
.el-node-form-tag {
  position: absolute;
  top: 50%;
  margin-left: -15px;
  height: 40px;
  width: 15px;
  background-color: #fbfbfb;
  border: 1px solid rgb(220, 227, 232);
  border-right: none;
  z-index: 0;
}
</style>
