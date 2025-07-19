package me.rerere.rikkahub.ui.pages.setting.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.rerere.rikkahub.R
import me.rerere.rikkahub.ui.components.ui.FormItem
import me.rerere.tts.provider.TTSProviderSetting

@Composable
fun TTSProviderConfigure(
  setting: TTSProviderSetting,
  modifier: Modifier = Modifier,
  onValueChange: (TTSProviderSetting) -> Unit
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier.verticalScroll(rememberScrollState())
  ) {
    // Provider type selector
    var expanded by remember { mutableStateOf(false) }
    val providers = remember { TTSProviderSetting.Types }

    FormItem(
      label = { Text(stringResource(R.string.setting_tts_page_provider_type)) },
      description = { Text(stringResource(R.string.setting_tts_page_provider_type_description)) },
    ) {
      ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
      ) {
        OutlinedTextField(
          value = when (setting) {
            is TTSProviderSetting.OpenAI -> "OpenAI"
            is TTSProviderSetting.Gemini -> "Gemini"
            is TTSProviderSetting.SystemTTS -> "System TTS"
          },
          onValueChange = {},
          readOnly = true,
          trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
          },
          modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false }
        ) {
          providers.forEach { providerClass ->
            DropdownMenuItem(
              text = {
                Text(
                  when (providerClass) {
                    TTSProviderSetting.OpenAI::class -> "OpenAI"
                    TTSProviderSetting.Gemini::class -> "Gemini"
                    TTSProviderSetting.SystemTTS::class -> "System TTS"
                    else -> providerClass.simpleName ?: "Unknown"
                  }
                )
              },
              onClick = {
                expanded = false
                val newSetting = when (providerClass) {
                  TTSProviderSetting.OpenAI::class -> TTSProviderSetting.OpenAI(
                    id = setting.id,
                    enabled = setting.enabled,
                    name = "OpenAI TTS"
                  )

                  TTSProviderSetting.Gemini::class -> TTSProviderSetting.Gemini(
                    id = setting.id,
                    enabled = setting.enabled,
                    name = "Gemini TTS"
                  )

                  TTSProviderSetting.SystemTTS::class -> TTSProviderSetting.SystemTTS(
                    id = setting.id,
                    enabled = setting.enabled,
                    name = "System TTS"
                  )

                  else -> setting
                }
                onValueChange(newSetting)
              }
            )
          }
        }
      }
    }

    // Name
    FormItem(
      label = { Text(stringResource(R.string.setting_tts_page_name)) },
      description = { Text(stringResource(R.string.setting_tts_page_name_description)) }
    ) {
      OutlinedTextField(
        value = setting.name,
        onValueChange = { newName ->
          onValueChange(setting.copyProvider(name = newName))
        },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.setting_tts_page_name_placeholder)) }
      )
    }

    // Provider-specific fields
    when (setting) {
      is TTSProviderSetting.OpenAI -> OpenAITTSConfiguration(setting, onValueChange)
      is TTSProviderSetting.Gemini -> GeminiTTSConfiguration(setting, onValueChange)
      is TTSProviderSetting.SystemTTS -> SystemTTSConfiguration(setting, onValueChange)
    }
  }
}

@Composable
private fun OpenAITTSConfiguration(
  setting: TTSProviderSetting.OpenAI,
  onValueChange: (TTSProviderSetting) -> Unit
) {
  // API Key
  FormItem(
    label = { Text(stringResource(R.string.setting_tts_page_api_key)) },
    description = { Text(stringResource(R.string.setting_tts_page_api_key_description)) }
  ) {
    OutlinedTextField(
      value = setting.apiKey,
      onValueChange = { newApiKey ->
        onValueChange(setting.copy(apiKey = newApiKey))
      },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("sk-...") },
    )
  }

  // Base URL
  FormItem(
    label = { Text(stringResource(R.string.setting_tts_page_base_url)) },
    description = { Text(stringResource(R.string.setting_tts_page_base_url_description)) }
  ) {
    OutlinedTextField(
      value = setting.baseUrl,
      onValueChange = { newBaseUrl ->
        onValueChange(setting.copy(baseUrl = newBaseUrl))
      },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("https://api.openai.com/v1") }
    )
  }

  // Model
  FormItem(
    label = { Text(stringResource(R.string.setting_tts_page_model)) },
    description = { Text(stringResource(R.string.setting_tts_page_model_description)) }
  ) {
    OutlinedTextField(
      value = setting.model,
      onValueChange = { newModel ->
        onValueChange(setting.copy(model = newModel))
      },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("gpt-4o-mini-tts") }
    )
  }

  // Voice
  var voiceExpanded by remember { mutableStateOf(false) }
  val voices = listOf("alloy", "echo", "fable", "onyx", "nova", "shimmer")

  FormItem(
    label = { Text(stringResource(R.string.setting_tts_page_voice)) },
    description = { Text(stringResource(R.string.setting_tts_page_voice_description)) }
  ) {
    ExposedDropdownMenuBox(
      expanded = voiceExpanded,
      onExpandedChange = { voiceExpanded = !voiceExpanded }
    ) {
      OutlinedTextField(
        value = setting.voice,
        onValueChange = { newVoice ->
          onValueChange(setting.copy(voice = newVoice))
        },
        modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(MenuAnchorType.PrimaryEditable),
        trailingIcon = {
          ExposedDropdownMenuDefaults.TrailingIcon(expanded = voiceExpanded)
        }
      )
      ExposedDropdownMenu(
        expanded = voiceExpanded,
        onDismissRequest = { voiceExpanded = false }
      ) {
        voices.forEach { voice ->
          DropdownMenuItem(
            text = { Text(voice) },
            onClick = {
              voiceExpanded = false
              onValueChange(setting.copy(voice = voice))
            }
          )
        }
      }
    }
  }

  // Speed
  FormItem(
    label = { Text(stringResource(R.string.setting_tts_page_speed)) },
    description = { Text(stringResource(R.string.setting_tts_page_speed_description)) }
  ) {
    OutlinedTextField(
      value = setting.speed.toString(),
      onValueChange = { newSpeed ->
        newSpeed.toFloatOrNull()?.let { speed ->
          if (speed in 0.25f..4.0f) {
            onValueChange(setting.copy(speed = speed))
          }
        }
      },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("1.0") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
  }
}

@Composable
private fun GeminiTTSConfiguration(
  setting: TTSProviderSetting.Gemini,
  onValueChange: (TTSProviderSetting) -> Unit
) {
  // API Key
  FormItem(
    label = { Text(stringResource(R.string.setting_tts_page_api_key)) },
    description = { Text(stringResource(R.string.setting_tts_page_api_key_description)) }
  ) {
    OutlinedTextField(
      value = setting.apiKey,
      onValueChange = { newApiKey ->
        onValueChange(setting.copy(apiKey = newApiKey))
      },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("AIza...") },
    )
  }

  // Model
  FormItem(
    label = { Text(stringResource(R.string.setting_tts_page_model)) },
    description = { Text(stringResource(R.string.setting_tts_page_model_description)) }
  ) {
    OutlinedTextField(
      value = setting.model,
      onValueChange = { newModel ->
        onValueChange(setting.copy(model = newModel))
      },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("gemini-2.5-flash-preview-tts") }
    )
  }

  // Voice Name
  FormItem(
    label = { Text(stringResource(R.string.setting_tts_page_voice_name)) },
    description = { Text(stringResource(R.string.setting_tts_page_voice_name_description)) }
  ) {
    OutlinedTextField(
      value = setting.voiceName,
      onValueChange = { newVoiceName ->
        onValueChange(setting.copy(voiceName = newVoiceName))
      },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("Kore") }
    )
  }
}

@Composable
private fun SystemTTSConfiguration(
  setting: TTSProviderSetting.SystemTTS,
  onValueChange: (TTSProviderSetting) -> Unit
) {
  // Speech Rate
  FormItem(
    label = { Text(stringResource(R.string.setting_tts_page_speech_rate)) },
    description = { Text(stringResource(R.string.setting_tts_page_speech_rate_description)) }
  ) {
    OutlinedTextField(
      value = setting.speechRate.toString(),
      onValueChange = { newRate ->
        newRate.toFloatOrNull()?.let { rate ->
          if (rate in 0.1f..3.0f) {
            onValueChange(setting.copy(speechRate = rate))
          }
        }
      },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("1.0") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
  }

  // Pitch
  FormItem(
    label = { Text(stringResource(R.string.setting_tts_page_pitch)) },
    description = { Text(stringResource(R.string.setting_tts_page_pitch_description)) }
  ) {
    OutlinedTextField(
      value = setting.pitch.toString(),
      onValueChange = { newPitch ->
        newPitch.toFloatOrNull()?.let { pitch ->
          if (pitch in 0.1f..2.0f) {
            onValueChange(setting.copy(pitch = pitch))
          }
        }
      },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("1.0") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
  }

  // Language
  FormItem(
    label = { Text(stringResource(R.string.setting_tts_page_language)) },
    description = { Text(stringResource(R.string.setting_tts_page_language_description)) }
  ) {
    OutlinedTextField(
      value = setting.language,
      onValueChange = { newLanguage ->
        onValueChange(setting.copy(language = newLanguage))
      },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("en-US") }
    )
  }
}