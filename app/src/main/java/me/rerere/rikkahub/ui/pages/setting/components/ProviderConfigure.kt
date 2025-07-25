package me.rerere.rikkahub.ui.pages.setting.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.rerere.ai.provider.ProviderSetting
import me.rerere.rikkahub.R
import kotlin.reflect.full.primaryConstructor

@Composable
fun ProviderConfigure(
    provider: ProviderSetting,
    modifier: Modifier = Modifier,
    onEdit: (provider: ProviderSetting) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        // Type
        if (!provider.builtIn) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                ProviderSetting.Types.forEachIndexed { index, type ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ProviderSetting.Types.size
                        ),
                        label = {
                            Text(type.simpleName ?: "")
                        },
                        selected = provider::class == type,
                        onClick = {
                            onEdit(type.primaryConstructor?.callBy(emptyMap())!!)
                        }
                    )
                }
            }
        }

        // [!] just for debugging
        // Text(JsonInstant.encodeToString(provider), fontSize = 10.sp)

        // Provider Configure
        when (provider) {
            is ProviderSetting.OpenAI -> {
                ProviderConfigureOpenAI(provider, onEdit)
            }

            is ProviderSetting.Google -> {
                ProviderConfigureGoogle(provider, onEdit)
            }

            is ProviderSetting.Claude -> {
                ProviderConfigureClaude(provider, onEdit)
            }
        }
    }
}

@Composable
private fun ColumnScope.ProviderConfigureOpenAI(
    provider: ProviderSetting.OpenAI,
    onEdit: (provider: ProviderSetting.OpenAI) -> Unit
) {
    provider.description()

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(id = R.string.setting_provider_page_enable), modifier = Modifier.weight(1f))
        Checkbox(
            checked = provider.enabled,
            onCheckedChange = {
                onEdit(provider.copy(enabled = it))
            }
        )
    }

    OutlinedTextField(
        value = provider.name,
        onValueChange = {
            onEdit(provider.copy(name = it.trim()))
        },
        label = {
            Text(stringResource(id = R.string.setting_provider_page_name))
        },
        modifier = Modifier.fillMaxWidth(),
    )

    OutlinedTextField(
        value = provider.apiKey,
        onValueChange = {
            onEdit(provider.copy(apiKey = it.trim()))
        },
        label = {
            Text(stringResource(id = R.string.setting_provider_page_api_key))
        },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 3,
    )

    OutlinedTextField(
        value = provider.baseUrl,
        onValueChange = {
            onEdit(provider.copy(baseUrl = it.trim()))
        },
        label = {
            Text(stringResource(id = R.string.setting_provider_page_api_base_url))
        },
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(id = R.string.setting_provider_page_response_api), modifier = Modifier.weight(1f))
        Checkbox(
            checked = provider.useResponseApi,
            onCheckedChange = {
                onEdit(provider.copy(useResponseApi = it))
            }
        )
    }
}

@Composable
private fun ColumnScope.ProviderConfigureClaude(
    provider: ProviderSetting.Claude,
    onEdit: (provider: ProviderSetting.Claude) -> Unit
) {
    provider.description()

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(id = R.string.setting_provider_page_enable), modifier = Modifier.weight(1f))
        Checkbox(
            checked = provider.enabled,
            onCheckedChange = {
                onEdit(provider.copy(enabled = it))
            }
        )
    }

    OutlinedTextField(
        value = provider.name,
        onValueChange = {
            onEdit(provider.copy(name = it.trim()))
        },
        label = {
            Text(stringResource(id = R.string.setting_provider_page_name))
        },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 3,
    )

    OutlinedTextField(
        value = provider.apiKey,
        onValueChange = {
            onEdit(provider.copy(apiKey = it.trim()))
        },
        label = {
            Text(stringResource(id = R.string.setting_provider_page_api_key))
        },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = provider.baseUrl,
        onValueChange = {
            onEdit(provider.copy(baseUrl = it.trim()))
        },
        label = {
            Text(stringResource(id = R.string.setting_provider_page_api_base_url))
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ColumnScope.ProviderConfigureGoogle(
    provider: ProviderSetting.Google,
    onEdit: (provider: ProviderSetting.Google) -> Unit
) {
    provider.description()

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(id = R.string.setting_provider_page_enable), modifier = Modifier.weight(1f))
        Checkbox(
            checked = provider.enabled,
            onCheckedChange = {
                onEdit(provider.copy(enabled = it))
            }
        )
    }

    OutlinedTextField(
        value = provider.name,
        onValueChange = {
            onEdit(provider.copy(name = it.trim()))
        },
        label = {
            Text(stringResource(id = R.string.setting_provider_page_name))
        },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = provider.apiKey,
        onValueChange = {
            onEdit(provider.copy(apiKey = it.trim()))
        },
        label = {
            Text(stringResource(id = R.string.setting_provider_page_api_key))
        },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 3,
    )

    if (!provider.vertexAI) {
        OutlinedTextField(
            value = provider.baseUrl,
            onValueChange = {
                onEdit(provider.copy(baseUrl = it.trim()))
            },
            label = {
                Text(stringResource(id = R.string.setting_provider_page_api_base_url))
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(id = R.string.setting_provider_page_vertex_ai), modifier = Modifier.weight(1f))
        Checkbox(
            checked = provider.vertexAI,
            onCheckedChange = {
                onEdit(provider.copy(vertexAI = it))
            }
        )
    }

    if (provider.vertexAI) {
        OutlinedTextField(
            value = provider.location,
            onValueChange = {
                onEdit(provider.copy(location = it.trim()))
            },
            label = {
                // https://cloud.google.com/vertex-ai/generative-ai/docs/learn/locations#available-regions
                Text(stringResource(id = R.string.setting_provider_page_location))
            },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = provider.projectId,
            onValueChange = {
                onEdit(provider.copy(projectId = it.trim()))
            },
            label = {
                Text(stringResource(id = R.string.setting_provider_page_project_id))
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
