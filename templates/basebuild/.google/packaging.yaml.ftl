# GOOGLE SAMPLE PACKAGING DATA
#
# This file is used by Google as part of our samples packaging process.
# End users may safely ignore this file. It has no relevance to other systems.
---
status:       <#if sample.metadata.status?is_node>${sample.metadata.status}<#else>DRAFT</#if>
technologies: [<#if sample.metadata.technologies?is_node>${sample.metadata.technologies}</#if>]
categories:   [<#if sample.metadata.categories?is_node>${sample.metadata.categories}</#if>]
languages:    [<#if sample.metadata.languages?is_node>${sample.metadata.languages}</#if>]
solutions:    [<#if sample.metadata.solutions?is_node>${sample.metadata.solutions}</#if>]
<#if sample.name??>
github:       android-${sample.name}
</#if><#if sample.metadata.level?is_node>
level:        ${sample.metadata.level}
</#if><#if sample.metadata.icon?is_node>
icon:         ${sample.metadata.icon}
</#if><#if sample.metadata.api_refs?is_node>
api_refs:
<#list sample.metadata.api_refs.android as ref>
    - android:${ref}
</#list>
<#list sample.metadata.api_refs.ext as ref>
    - ${ref}
</#list></#if>
license: apache2
