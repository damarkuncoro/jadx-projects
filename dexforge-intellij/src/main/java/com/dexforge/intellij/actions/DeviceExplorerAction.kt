package com.dexforge.intellij.actions

import com.dexforge.intellij.services.DeviceExplorerService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import javax.swing.JComponent
import javax.swing.DefaultListModel

class DeviceExplorerAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val deviceService = DeviceExplorerService.getInstance(project)

        val devices = deviceService.listDevices()
        if (devices.isEmpty()) {
            Messages.showWarningDialog(
                project,
                "No Android devices found. Make sure ADB is running and a device is connected.",
                "DexForge Device Explorer"
            )
            return
        }

        DeviceExplorerDialog(project, devices, deviceService).show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}

class DeviceExplorerDialog(
    private val project: Project,
    private val devices: List<DeviceExplorerService.DeviceInfo>,
    private val deviceService: DeviceExplorerService
) : DialogWrapper(project) {
    private val deviceListModel = DefaultListModel<String>()
    private val packageListModel = DefaultListModel<String>()

    init {
        title = "DexForge Device Explorer"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val deviceList = JBList(deviceListModel)
        val packageList = JBList(packageListModel)

        devices.forEach { device ->
            deviceListModel.addElement("${device.model} (${device.serial}) - Android ${device.androidVersion}")
        }

        deviceList.addListSelectionListener {
            if (!deviceList.valueIsAdjusting) {
                val selectedIndex = deviceList.selectedIndex
                if (selectedIndex >= 0) {
                    loadPackages(devices[selectedIndex].serial)
                }
            }
        }

        val leftPanel = JBScrollPane(deviceList)
        val rightPanel = JBScrollPane(packageList)

        leftPanel.border = javax.swing.BorderFactory.createTitledBorder("Devices")
        rightPanel.border = javax.swing.BorderFactory.createTitledBorder("Packages")

        val splitPane = javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel)
        splitPane.resizeWeight = 0.5
        return splitPane
    }

    private fun loadPackages(deviceSerial: String) {
        packageListModel.clear()
        val packages = deviceService.listPackages(deviceSerial)
        packages.forEach { pkg ->
            val splitInfo = if (pkg.isSplit) " [${pkg.splitType}]" else ""
            packageListModel.addElement("${pkg.packageName}$splitInfo")
        }
    }

    override fun doOKAction() {
        super.doOKAction()
    }
}
