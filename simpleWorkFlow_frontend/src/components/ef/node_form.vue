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
          <el-form-item label="类型">
            <el-input v-model="node.type" :disabled="true"></el-input>
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model="node.name"></el-input>
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

          <el-form-item label="网关到达" v-show="node.gateway">
            <el-input
              onkeyup="value=value.replace(/[^\d]/g,'')"
              v-model="node.arrivalCnt"
            ></el-input>
          </el-form-item>

          <el-form-item label="状态">
            <el-select v-model="node.state" placeholder="请选择">
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
          v-show="type === 'line'"
        >
          <el-form-item label="条件">
            <el-input v-model="line.label"></el-input>
          </el-form-item>
          <el-form-item label="优先级">
            <el-input
              type="text"
              onkeyup="value=value.replace(/[^\d]/g,'')"
              size="small"
              width="50"
              v-model="line.exclusiveOrder"
              placeholder="请输入数字"
            ></el-input>
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
      stateList: [
        {
          state: "success",
          label: "成功",
        },
        {
          state: "warning",
          label: "警告",
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
      console.log("line", line);
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
      //   this.$emit("$message.succss","保存成功")
    },
    save() {
      this.data.nodeList.filter((node) => {
        if (node.id === this.node.id) {
          node.name = this.node.name;
          node.left = this.node.left;
          node.top = this.node.top;
          node.ico = this.node.ico;
          node.state = this.node.state;
          node.arrivalCnt = this.node.arrivalCnt;
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
