package me.rerere.rikkahub.ui.pages.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.DatabaseBackup
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.File
import com.composables.icons.lucide.Import
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Upload
import com.dokar.sonner.ToastType
import kotlinx.coroutines.launch
import me.rerere.rikkahub.data.datastore.WebDavConfig
import me.rerere.rikkahub.data.sync.BackupFileItem
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.FormItem
import me.rerere.rikkahub.ui.components.ui.StickyHeader
import me.rerere.rikkahub.ui.context.LocalToaster
import me.rerere.rikkahub.utils.UiState
import me.rerere.rikkahub.utils.fileSizeToString
import me.rerere.rikkahub.utils.onError
import me.rerere.rikkahub.utils.onLoading
import me.rerere.rikkahub.utils.onSuccess
import me.rerere.rikkahub.utils.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun BackupPage(vm: BackupVM = koinViewModel()) {
    val pagerState = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("备份与恢复")
                },
                navigationIcon = {
                    BackButton()
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    icon = {
                        Icon(Lucide.DatabaseBackup, null)
                    },
                    label = {
                        Text("WebDav备份")
                    },
                    onClick = {
                        scope.launch { pagerState.scrollToPage(0) }
                    },
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    icon = {
                        Icon(Lucide.Import, null)
                    },
                    label = {
                        Text("导入和导出")
                    },
                    onClick = {
                        scope.launch { pagerState.scrollToPage(1) }
                    },
                )
            }
        }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = it
        ) { page ->
            when (page) {
                0 -> {
                    WebDavPage(vm)
                }

                1 -> {
                    ImportExportPage(vm)
                }
            }
        }
    }
}

@Composable
private fun WebDavPage(
    vm: BackupVM
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val webDavConfig = settings.webDavConfig
    val toaster = LocalToaster.current
    val scope = rememberCoroutineScope()
    var showBackupFiles by remember { mutableStateOf(false) }

    fun updateWebDavConfig(newConfig: WebDavConfig) {
        vm.updateSettings(settings.copy(webDavConfig = newConfig))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormItem(
                    label = { Text("WebDAV 服务器地址") }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = webDavConfig.url,
                        onValueChange = { updateWebDavConfig(webDavConfig.copy(url = it.trim())) },
                        placeholder = { Text("https://example.com/dav") },
                        singleLine = true
                    )
                }
                FormItem(
                    label = { Text("用户名") }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = webDavConfig.username,
                        onValueChange = {
                            updateWebDavConfig(
                                webDavConfig.copy(
                                    username = it.trim()
                                )
                            )
                        },
                        singleLine = true
                    )
                }
                FormItem(
                    label = { Text("密码") }
                ) {
                    var passwordVisible by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = webDavConfig.password,
                        onValueChange = { updateWebDavConfig(webDavConfig.copy(password = it)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Lucide.EyeOff
                            else
                                Lucide.Eye
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, null)
                            }
                        },
                        singleLine = true
                    )
                }
                FormItem(
                    label = { Text("路径") }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = webDavConfig.path,
                        onValueChange = { updateWebDavConfig(webDavConfig.copy(path = it.trim())) },
                        singleLine = true
                    )
                }
            }
        }

        Card {
            FormItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                label = {
                    Text("备份项目")
                }
            ) {
                MultiChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    WebDavConfig.BackupItem.entries.forEachIndexed { index, item ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = WebDavConfig.BackupItem.entries.size
                            ),
                            onCheckedChange = {
                                val newItems = if (it) {
                                    webDavConfig.items + item
                                } else {
                                    webDavConfig.items - item
                                }
                                updateWebDavConfig(webDavConfig.copy(items = newItems))
                            },
                            checked = item in webDavConfig.items
                        ) {
                            Text(
                                when (item) {
                                    WebDavConfig.BackupItem.DATABASE -> "聊天记录"
                                    WebDavConfig.BackupItem.FILES -> "文件"
                                }
                            )
                        }
                    }
                }
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        try {
                            vm.testWebDav()
                            toaster.show("连接成功", type = ToastType.Success)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            toaster.show("连接失败: ${e.message}", type = ToastType.Error)
                        }
                    }
                }
            ) {
                Text("测试连接")
            }
            OutlinedButton(
                onClick = {
                    showBackupFiles = true
                }
            ) {
                Text("恢复")
            }

            Button(
                onClick = {
                    scope.launch {
                        runCatching {
                            vm.backup()
                            toaster.show("备份成功", type = ToastType.Success)
                        }
                            .onFailure {
                                it.printStackTrace()
                                toaster.show(it.message ?: "未知错误", type = ToastType.Error)
                            }
                    }
                }
            ) {
                Icon(Lucide.Upload, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("立即备份")
            }
        }
    }

    if (showBackupFiles) {
        ModalBottomSheet(
            onDismissRequest = {
                showBackupFiles = false
            },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("WebDav备份文件列表", modifier = Modifier.fillMaxWidth())
                val backupItems by remember { vm.getWebDavBackupFiles() }
                    .collectAsStateWithLifecycle(initialValue = UiState.Idle)
                backupItems.onSuccess {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(it) { item ->
                            BackupItemCard(
                                item = item,
                                onDelete = {

                                },
                                onRestore = { item ->
                                    scope.launch {
                                        runCatching {
                                            vm.restore(item = item)
                                            toaster.show("恢复成功", type = ToastType.Success)
                                            showBackupFiles = false
                                        }.onFailure { err ->
                                            err.printStackTrace()
                                            toaster.show(
                                                err.message ?: "未知错误",
                                                type = ToastType.Error
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }.onError {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "加载备份文件失败: ${it.message}", color = Color.Red)
                    }
                }.onLoading {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularWavyProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupItemCard(
    item: BackupFileItem,
    onDelete: (BackupFileItem) -> Unit = {},
    onRestore: (BackupFileItem) -> Unit = {}
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.lastModified.toLocalDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = item.size.fileSizeToString(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    onDelete(item)
                }
            ) {
                Text("删除")
            }
            Button(
                onClick = {
                    onRestore(item)
                }
            ) {
                Text("恢复")
            }
        }
    }
}

@Composable
private fun ImportExportPage(
    vm: BackupVM
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        stickyHeader {
            StickyHeader {
                Text("导出RikkaHub数据")
            }
        }

        item {
            Card {
                ListItem(
                    headlineContent = {
                        Text("导出为文件")
                    },
                    supportingContent = {
                        Text("导出APP数据为文件")
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = {
                        Icon(Lucide.File, null)
                    }
                )
            }
        }

        stickyHeader {
            StickyHeader {
                Text("导入其他应用数据")
            }
        }

        item {
            Card {
                ListItem(
                    headlineContent = {
                        Text("从ChatBox导入")
                    },
                    supportingContent = {
                        Text("导入ChatBox数据")
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = {
                        Icon(Lucide.Import, null)
                    }
                )
            }
        }
    }
}