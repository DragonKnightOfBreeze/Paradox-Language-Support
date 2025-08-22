{# @pebvariable name="request" type="icu.windea.pls.ai.requests.ManipulateLocalisationAiRequest" #}
{# @pebvariable name="eol" type="java.lang.String" #}
上下文信息：
{% if request.context.filePath %}
- 当前文件路径：{{ request.context.filePath }}{{ eol }}
{% endif %}
{% if request.context.fileName %}
- 当前文件名：{{ request.context.fileName }}{{ eol }}
{% endif %}
{% if request.context.modName %}
- 当前模组名：{{ request.context.modName }}{{ eol }}