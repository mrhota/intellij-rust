package org.rust.cargo.project

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.Key
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.externalSystem.service.project.manage.AbstractProjectDataService
import com.intellij.openapi.externalSystem.util.Order
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project

class CargoProjectDataService : AbstractProjectDataService<ModuleData, Module>() {
    override fun getTargetDataKey(): Key<ModuleData> = ProjectKeys.MODULE

    override fun postProcess(toImport: Collection<DataNode<ModuleData>>,
                             projectData: ProjectData?,
                             project: Project,
                             modelsProvider: IdeModifiableModelsProvider) {
        println("Post processing!")
        toImport.forEach {
            println(it)
        }
    }
}
