{{- define "fusion-arch.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "fusion-arch.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- include "fusion-arch.name" . -}}
{{- end -}}
{{- end -}}

{{- define "fusion-arch.componentFullname" -}}
{{- printf "%s-%s" (include "fusion-arch.fullname" .root) .name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "fusion-arch.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
{{- default (include "fusion-arch.fullname" .) .Values.serviceAccount.name -}}
{{- else -}}
{{- default "default" .Values.serviceAccount.name -}}
{{- end -}}
{{- end -}}

{{- define "fusion-arch.labels" -}}
app.kubernetes.io/managed-by: {{ .root.Release.Service }}
app.kubernetes.io/instance: {{ .root.Release.Name }}
app.kubernetes.io/part-of: {{ include "fusion-arch.fullname" .root }}
helm.sh/chart: {{ printf "%s-%s" .root.Chart.Name .root.Chart.Version | replace "+" "_" }}
app.kubernetes.io/name: {{ include "fusion-arch.componentFullname" . }}
app.kubernetes.io/component: {{ .name }}
{{- end -}}

{{- define "fusion-arch.selectorLabels" -}}
app.kubernetes.io/instance: {{ .root.Release.Name }}
app.kubernetes.io/part-of: {{ include "fusion-arch.fullname" .root }}
app.kubernetes.io/component: {{ .name }}
{{- end -}}

