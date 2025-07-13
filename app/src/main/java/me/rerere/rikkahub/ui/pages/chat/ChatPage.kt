package me.rerere.rikkahub.ui.pages.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.window.core.layout.WindowSizeClass
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.History
import com.composables.icons.lucide.ListTree
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.MessageCirclePlus
import com.composables.icons.lucide.Settings
import com.dokar.sonner.ToastType
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.rerere.ai.provider.Model
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.BuildConfig
import me.rerere.rikkahub.R
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.data.datastore.findProvider
import me.rerere.rikkahub.data.datastore.getCurrentAssistant
import me.rerere.rikkahub.data.datastore.getCurrentChatModel
import me.rerere.rikkahub.data.model.Conversation
import me.rerere.rikkahub.data.repository.ConversationRepository
import me.rerere.rikkahub.ui.components.chat.AssistantPicker
import me.rerere.rikkahub.ui.components.chat.ChatInput
import me.rerere.rikkahub.ui.components.chat.Greeting
import me.rerere.rikkahub.ui.components.chat.rememberChatInputState
import me.rerere.rikkahub.ui.components.chat.UIAvatar
import me.rerere.rikkahub.ui.components.richtext.MarkdownBlock
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.context.LocalToaster
import me.rerere.rikkahub.ui.context.push
import me.rerere.rikkahub.ui.hooks.EditStateContent
import me.rerere.rikkahub.ui.hooks.useEditState
import me.rerere.rikkahub.ui.hooks.useThrottle
import me.rerere.rikkahub.utils.UpdateDownload
import me.rerere.rikkahub.utils.Version
import me.rerere.rikkahub.utils.base64Decode
import me.rerere.rikkahub.utils.navigateToChatPage
import me.rerere.rikkahub.utils.onError
import me.rerere.rikkahub.utils.onSuccess
import me.rerere.rikkahub.utils.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.uuid.Uuid

@Composable
fun ChatPage(id: Uuid, text: String?) {
  val vm: ChatVM = koinViewModel(
    parameters = {
      parametersOf(id.toString())
    }
  )
  val navController = LocalNavController.current
  val toaster = LocalToaster.current
  val scope = rememberCoroutineScope()

  // Handle Error
  LaunchedEffect(Unit) {
    vm.errorFlow.collect { error ->
      toaster.show(error.message ?: "Error", type = ToastType.Error)
    }
  }

  val setting by vm.settings.collectAsStateWithLifecycle()
  val conversations by vm.conversations.collectAsStateWithLifecycle()
  val conversation by vm.conversation.collectAsStateWithLifecycle()
  val loadingJob by vm.conversationJob.collectAsStateWithLifecycle()
  val currentChatModel by vm.currentChatModel.collectAsStateWithLifecycle()
  val enableWebSearch by vm.enableWebSearch.collectAsStateWithLifecycle()

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

  val windowAdaptiveInfo = currentWindowAdaptiveInfo()

  when {
    windowAdaptiveInfo.windowSizeClass
      .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> {
      PermanentNavigationDrawer(
        drawerContent = {
          DrawerContent(
            navController = navController,
            current = conversation,
            conversations = conversations,
            loading = loadingJob != null,
            vm = vm,
            settings = setting
          )
        }
      ) {
        ChatPageContent(
          text = text,
          loadingJob = loadingJob,
          setting = setting,
          conversation = conversation,
          drawerState = drawerState,
          navController = navController,
          vm = vm,
          enableWebSearch = enableWebSearch,
          currentChatModel = currentChatModel,
        )
      }
    }

    else -> {
      ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
          DrawerContent(
            navController = navController,
            current = conversation,
            conversations = conversations,
            loading = loadingJob != null,
            vm = vm,
            settings = setting
          )
        }
      ) {
        ChatPageContent(
          text = text,
          loadingJob = loadingJob,
          setting = setting,
          conversation = conversation,
          drawerState = drawerState,
          navController = navController,
          vm = vm,
          enableWebSearch = enableWebSearch,
          currentChatModel = currentChatModel,
        )
      }
    }
  }
}

@Composable
private fun ChatPageContent(
  text: String?,
  loadingJob: Job?,
  setting: Settings,
  conversation: Conversation,
  drawerState: DrawerState,
  navController: NavBackStack,
  vm: ChatVM,
  enableWebSearch: Boolean,
  currentChatModel: Model?,
) {
  val inputState = rememberChatInputState(
    message = text?.let {
      listOf(UIMessagePart.Text(it.base64Decode()))
    } ?: emptyList(),
  )
  val scope = rememberCoroutineScope()
  val toaster = LocalToaster.current
  LaunchedEffect(loadingJob) {
    inputState.loading = loadingJob != null
  }
  val chatListState = rememberLazyListState()
  Scaffold(
    topBar = {
      TopBar(
        settings = setting,
        conversation = conversation,
        drawerState = drawerState,
        onNewChat = {
          navigateToChatPage(navController)
        },
        onClickMenu = {
          navController.push(Screen.Menu)
        },
        onUpdateTitle = {
          vm.updateTitle(it)
        }
      )
    },
    bottomBar = {
      ChatInput(
        state = inputState,
        settings = setting,
        conversation = conversation,
        mcpManager = vm.mcpManager,
        onCancelClick = {
          loadingJob?.cancel()
        },
        enableSearch = enableWebSearch,
        onToggleSearch = {
          vm.updateSettings(setting.copy(enableWebSearch = !enableWebSearch))
        },
        onSendClick = {
          if (currentChatModel == null) {
            toaster.show("请先选择模型", type = ToastType.Error)
            return@ChatInput
          }
          if (inputState.isEditing()) {
            vm.handleMessageEdit(
              parts = inputState.messageContent,
              messageId = inputState.editingMessage!!,
            )
          } else {
            vm.handleMessageSend(inputState.messageContent)
          }
          inputState.clearInput()
          scope.launch {
            chatListState.scrollToItem(conversation.currentMessages.size + 1)
          }
        },
        onUpdateChatModel = {
          vm.setChatModel(assistant = setting.getCurrentAssistant(), model = it)
        },
        onUpdateAssistant = {
          vm.updateSettings(
            setting.copy(
              assistants = setting.assistants.map { assistant ->
                if (assistant.id == it.id) {
                  it
                } else {
                  assistant
                }
              }
            )
          )
        },
        onUpdateSearchService = { index ->
          vm.updateSettings(
            setting.copy(
              searchServiceSelected = index
            )
          )
        },
        onClearContext = {
          vm.handleMessageTruncate()
        },
      )
    }
  ) { innerPadding ->
    ChatList(
      innerPadding = innerPadding,
      conversation = conversation,
      state = chatListState,
      loading = loadingJob != null,
      settings = setting,
      onRegenerate = {
        vm.regenerateAtMessage(it)
      },
      onEdit = {
        inputState.editingMessage = it.id
        inputState.messageContent = it.parts
      },
      onForkMessage = {
        scope.launch {
          val fork = vm.forkMessage(message = it)
          navigateToChatPage(navController, chatId = fork.id)
        }
      },
      onDelete = {
        vm.deleteMessage(it)
      },
      onUpdateMessage = { newNode ->
        vm.updateConversation(
          conversation.copy(
            messageNodes = conversation.messageNodes.map { node ->
              if (node.id == newNode.id) {
                newNode
              } else {
                node
              }
            }
          ))
        vm.saveConversationAsync()
      },
      onClickSuggestion = { suggestion ->
        inputState.editingMessage = null
        inputState.messageContent = listOf(
          UIMessagePart.Text(
            text = suggestion
          )
        )
      }
    )
  }
}

@Composable
private fun TopBar(
  settings: Settings,
  conversation: Conversation,
  drawerState: DrawerState,
  onClickMenu: () -> Unit,
  onNewChat: () -> Unit,
  onUpdateTitle: (String) -> Unit
) {
  val scope = rememberCoroutineScope()
  val toaster = LocalToaster.current
  val titleState = useEditState<String> {
    onUpdateTitle(it)
  }
  val windowAdaptiveInfo = currentWindowAdaptiveInfo().windowSizeClass

  TopAppBar(
    navigationIcon = {
      if (!windowAdaptiveInfo.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        IconButton(
          onClick = {
            scope.launch { drawerState.open() }
          }
        ) {
          Icon(Lucide.ListTree, "Messages")
        }
      }
    },
    title = {
      val editTitleWarning = stringResource(R.string.chat_page_edit_title_warning)
      Surface(
        onClick = {
          if (conversation.messageNodes.isNotEmpty()) {
            titleState.open(conversation.title)
          } else {
            toaster.show(editTitleWarning, type = ToastType.Warning)
          }
        }
      ) {
        Column {
          val assistant = settings.getCurrentAssistant()
          val model = settings.getCurrentChatModel()
          val provider = model?.findProvider(settings.providers)
          Text(
            text = conversation.title.ifBlank { stringResource(R.string.chat_page_new_chat) },
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
          )
          if (model != null && provider != null) {
            Text(
              text = "${assistant.name.ifBlank { stringResource(R.string.assistant_page_default_assistant) }} / ${model.displayName} (${provider.name})",
              overflow = TextOverflow.Ellipsis,
              maxLines = 1,
              color = LocalContentColor.current.copy(0.65f),
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
              )
            )
          }
        }
      }
    },
    actions = {
      IconButton(
        onClick = {
          onClickMenu()
        }
      ) {
        Icon(Lucide.Menu, "Menu")
      }

      IconButton(
        onClick = {
          onNewChat()
        }
      ) {
        Icon(Lucide.MessageCirclePlus, "New Message")
      }
    },
  )
  titleState.EditStateContent { title, onUpdate ->
    AlertDialog(
      onDismissRequest = {
        titleState.dismiss()
      },
      title = {
        Text(stringResource(R.string.chat_page_edit_title))
      },
      text = {
        OutlinedTextField(
          value = title,
          onValueChange = onUpdate,
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            titleState.confirm()
          }
        ) {
          Text(stringResource(R.string.chat_page_save))
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            titleState.dismiss()
          }
        ) {
          Text(stringResource(R.string.chat_page_cancel))
        }
      }
    )
  }
}

@Composable
private fun DrawerContent(
  navController: NavBackStack,
  vm: ChatVM,
  settings: Settings,
  current: Conversation,
  conversations: List<Conversation>,
  loading: Boolean,
) {
  val scope = rememberCoroutineScope()

  // 昵称编辑状态
  val nicknameEditState = useEditState<String> { newNickname ->
    vm.updateSettings(
      settings.copy(
        displaySetting = settings.displaySetting.copy(
          userNickname = newNickname
        )
      )
    )
  }

  ModalDrawerSheet(
    modifier = Modifier.width(300.dp)
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      // 用户头像和昵称自定义区域
      Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        UIAvatar(
          name = settings.displaySetting.userNickname.ifBlank { stringResource(R.string.user_default_name) },
          value = settings.displaySetting.userAvatar,
          onUpdate = { newAvatar ->
            vm.updateSettings(
              settings.copy(
                displaySetting = settings.displaySetting.copy(
                  userAvatar = newAvatar
                )
              )
            )
          },
          modifier = Modifier.size(64.dp),
        )

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = settings.displaySetting.userNickname.ifBlank { stringResource(R.string.user_default_name) },
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable {
              nicknameEditState.open(settings.displaySetting.userNickname)
            }
          )
          Greeting(
            style = MaterialTheme.typography.labelMedium,
          )
        }
      }

      if (settings.displaySetting.showUpdates) {
        UpdateCard(vm)
      }
      ConversationList(
        current = current,
        conversations = conversations,
        loadings = if (loading) listOf(current.id) else emptyList(),
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        onClick = {
          navigateToChatPage(navController, it.id)
        },
        onRegenerateTitle = {
          vm.generateTitle(it, true)
        },
        onDelete = {
          vm.deleteConversation(it)
          if (it.id == current.id) {
            navigateToChatPage(navController)
          }
        }
      )
      val repo = koinInject<ConversationRepository>()
      AssistantPicker(
        settings = settings,
        onUpdateSettings = {
          vm.updateSettings(it)
          scope.launch {
            val conversation = repo.getConversationsOfAssistant(it.assistantId)
              .first()
              .firstOrNull()
            navigateToChatPage(navController, conversation?.id ?: Uuid.random())
          }
        },
        modifier = Modifier.fillMaxWidth(),
        onClickSetting = {
          val currentAssistantId = settings.assistantId
          navController.push(Screen.AssistantDetail(id = currentAssistantId.toString()))
        }
      )
      Row(
        modifier = Modifier.fillMaxWidth()
      ) {
        TextButton(
          onClick = {
            navController.push(Screen.History)
          },
          modifier = Modifier.weight(1f)
        ) {
          Icon(Lucide.History, "Chat History")
          Text(
            stringResource(R.string.chat_page_history),
            modifier = Modifier.padding(start = 4.dp)
          )
        }
        TextButton(
          onClick = {
            navController.push(Screen.Setting)
          },
          modifier = Modifier.weight(1f)
        ) {
          Icon(Lucide.Settings, stringResource(R.string.settings))
          Text(
            stringResource(R.string.settings),
            modifier = Modifier.padding(start = 4.dp)
          )
        }
      }
    }
  }

  // 昵称编辑对话框
  nicknameEditState.EditStateContent { nickname, onUpdate ->
    AlertDialog(
      onDismissRequest = {
        nicknameEditState.dismiss()
      },
      title = {
        Text("编辑昵称")
      },
      text = {
        OutlinedTextField(
          value = nickname,
          onValueChange = onUpdate,
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          placeholder = { Text("请输入昵称") }
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            nicknameEditState.confirm()
          }
        ) {
          Text("保存")
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            nicknameEditState.dismiss()
          }
        ) {
          Text("取消")
        }
      }
    )
  }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun UpdateCard(vm: ChatVM) {
  val state by vm.updateState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val toaster = LocalToaster.current
  state.onError {
    Card {
      Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "检查更新失败",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.error
        )
        Text(
          text = it.message ?: "未知错误",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.error
        )
      }
    }
  }
  state.onSuccess { info ->
    var showDetail by remember { mutableStateOf(false) }
    val current = remember { Version(BuildConfig.VERSION_NAME) }
    val latest = remember(info) { Version(info.version) }
    if (latest > current) {
      Card(
        onClick = {
          showDetail = true
        }
      ) {
        Column(
          modifier = Modifier
              .padding(8.dp)
              .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "发现新版本 ${info.version}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
          )
          MarkdownBlock(
            content = info.changelog,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.heightIn(max = 400.dp)
          )
        }
      }
    }
    if (showDetail) {
      val downloadHandler = useThrottle<UpdateDownload>(500) { item ->
        vm.updateChecker.downloadUpdate(context, item)
        showDetail = false
        toaster.show("已在下载，请在状态栏查看下载进度", type = ToastType.Info)
      }
      ModalBottomSheet(
        onDismissRequest = { showDetail = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
      ) {
        Column(
          modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 32.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = info.version,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = Instant.parse(info.publishedAt).toJavaInstant().toLocalDateTime(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
          )
          MarkdownBlock(
            content = info.changelog,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .verticalScroll(rememberScrollState()),
            style = MaterialTheme.typography.bodyMedium
          )
          info.downloads.fastForEach { downloadItem ->
            OutlinedCard(
              onClick = {
                downloadHandler(downloadItem)
              },
            ) {
              ListItem(
                headlineContent = {
                  Text(
                    text = downloadItem.name,
                  )
                },
                supportingContent = {
                  Text(
                    text = downloadItem.size
                  )
                },
                leadingContent = {
                  Icon(
                    Lucide.Download,
                    contentDescription = null
                  )
                }
              )
            }
          }
        }
      }
    }
  }
}
