package com.zakl.workflow.core.service

import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson.JSON
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.zakl.workflow.core.WorkFlowState
import com.zakl.workflow.core.modeldefine.ModelChecker
import com.zakl.workflow.core.modeldefine.WorkFlowComponentBase
import com.zakl.workflow.core.entity.*
import com.zakl.workflow.core.modeldefine.WorkFlowNode
import com.zakl.workflow.exception.CustomException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val SERVICE_BEAN_NAME: String = "modelservice";

interface ModelService {
    /**
     * 更新model
     */
    fun insertOrUpdateConfig(modelInfo: ModelInfo): ModelConfig

    /**
     * 部署流程
     */
    fun deployModel(modelId: String)

    /**
     * 删除流程
     */
    fun deleteModel(modelId: String)

    /**
     * 查询流程信息
     */
    fun getModelConfigs(page: Int, size: Int, deployStatus: Int): Page<ModelConfig>


}

@Service(value = SERVICE_BEAN_NAME)
@Transactional
class ModelServiceImpl : ModelService {

    @Autowired
    lateinit var modelComponentMapper: ModelComponentMapper

    @Autowired
    lateinit var modelConfigMapper: ModelConfigMapper

    @Autowired
    lateinit var nodeRelService: NodeRelService

    @Autowired
    lateinit var processInstanceMapper: ProcessInstanceMapper


    override fun insertOrUpdateConfig(modelInfo: ModelInfo): ModelConfig {
        val modelId = modelInfo.modelId
        ModelChecker.modelCheck(modelInfo)
        val model: ModelConfig
        if (StrUtil.isNotBlank(modelId)) {
            model = modelConfigMapper.selectById(modelId) ?: throw CustomException.neSlf4jStyle(
                "modeId: {} can not found!",
                modelId!!
            )
            model.tmpModel = modelInfo.sourModelInfo
            model.name = modelInfo.name
            modelConfigMapper.updateById(model)
        } else {
            model = ModelConfig(modelInfo.sourModelInfo, modelInfo.name);
            modelConfigMapper.insert(model)
        }
        modelComponentMapper.delete(QueryWrapper<ModelComponent>().eq("modelId", model.id))

        modelInfo.run {
            val workFlowComponents = ArrayList<WorkFlowComponentBase>()
            workFlowComponents.addAll(this.nodes)
            workFlowComponents.addAll(this.lines)
            workFlowComponents.addAll(this.gateways)
            return@run workFlowComponents
        }.map { i -> ModelComponent(i.id, model.id!!, JSON.toJSONString(i), i.componentType) }
            .forEach(modelComponentMapper::insert)
        return model
    }

    override fun deployModel(modelId: String) {
        //如果现有流程尚未执行完毕,则不允许进行变更
        processInstanceMapper.selectList(
            QueryWrapper<ProcessInstance?>().eq("modelId", modelId).ne("workFlowState", WorkFlowState.DONE.code)
        ).run {
            if (this.isNotEmpty()) {
                throw CustomException.neSlf4jStyle("当前model: {} ,存在尚未完成的流程! 不可进行更新部署", modelId)
            }
        }

        modelConfigMapper.deployModel(modelId)
        nodeRelService.initModelComponents(
            modelId,
            modelComponentMapper.selectList(QueryWrapper<ModelComponent>().eq("modelId", modelId))
        )
    }

    override fun deleteModel(modelId: String) {
        modelConfigMapper.deleteById(modelId)
    }

    override fun getModelConfigs(page: Int, size: Int, deployStatus: Int): Page<ModelConfig> {
        var queryWrapper: QueryWrapper<ModelConfig> = QueryWrapper<ModelConfig>()
        if (deployStatus != -1) {
            queryWrapper = queryWrapper.eq("idDeploy", deployStatus == 1)
        }
        return modelConfigMapper.selectPage(Page(page.toLong(), size.toLong()), queryWrapper)
    }


}