{{- define "realitychecker.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "realitychecker.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{- define "realitychecker.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "realitychecker.labels" -}}
helm.sh/chart: {{ include "realitychecker.chart" . }}
app.kubernetes.io/name: {{ include "realitychecker.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "realitychecker.selectorLabels" -}}
app.kubernetes.io/name: {{ include "realitychecker.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "realitychecker.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "realitychecker.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{- define "realitychecker.secretName" -}}
{{- printf "%s-secrets" (include "realitychecker.fullname" .) }}
{{- end }}

{{- define "realitychecker.postgresqlSecretName" -}}
{{- default (include "realitychecker.secretName" .) .Values.postgresql.existingSecret }}
{{- end }}

{{- define "realitychecker.mailSecretName" -}}
{{- default (include "realitychecker.secretName" .) .Values.mail.existingSecret }}
{{- end }}
