{# @pebvariable name="request" type="icu.windea.pls.ai.requests.ManipulateLocalisationAiRequest" #}
{% if not request.context.isEmpty() %}

Context information:
{% if request.context.filePath %}
* Current file path: {{ request.context.filePath }}
{% endif %}
{% if request.context.fileName %}
* Current file name: {{ request.context.fileName }}
{% endif %}
{% if request.context.modName %}
* Current mod name: {{ request.context.modName }}
{% endif %}
{% endif %}