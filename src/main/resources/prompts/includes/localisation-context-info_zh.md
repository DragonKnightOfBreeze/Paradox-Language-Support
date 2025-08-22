{# @pebvariable name="request" type="icu.windea.pls.ai.requests.ManipulateLocalisationAiRequest" #}
上下文信息：
{% if request.context.filePath %}
- 当前文件路径: {{ request.context.filePath }}
{% endif %}
{% if request.context.fileName %}
- 当前文件名: {{ request.context.fileName }}
{% endif %}
{% if request.context.modName %}
- 当前模组名: {{ request.context.modName }}