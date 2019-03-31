package com.ephemeralin.jira.plugins.customfields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.SelectCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.search.SearchContextImpl;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class BooleanField extends SelectCFType {
    private static final Logger log = LoggerFactory.getLogger(BooleanField.class);
    private final OptionsManager optionsManager;

    @Inject
    public BooleanField(@ComponentImport CustomFieldValuePersister customFieldValuePersister,
                        @ComponentImport OptionsManager optionsManager,
                        @ComponentImport GenericConfigManager genericConfigManager,
                        @ComponentImport JiraBaseUrls jiraBaseUrls) {
        super(customFieldValuePersister, optionsManager, genericConfigManager, jiraBaseUrls);
        this.optionsManager = optionsManager;
    }

    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue,
                                                     final CustomField field,
                                                     final FieldLayoutItem fieldLayoutItem) {
        final Map<String, Object> parameters = super.getVelocityParameters(issue, field, fieldLayoutItem);
        FieldConfig fieldConfiguration;
        if (issue == null) {
            fieldConfiguration = field.getReleventConfig(new SearchContextImpl());
        } else {
            fieldConfiguration = field.getRelevantConfig(issue);
        }
        Options options = this.optionsManager.getOptions(fieldConfiguration);
        if (options.isEmpty()) {
            this.optionsManager.createOption(fieldConfiguration, null, 1L, "True");
            this.optionsManager.createOption(fieldConfiguration, null, 2L, "False");
        }
        options = this.optionsManager.getOptions(fieldConfiguration);
        Map<Long, String> results = new HashMap<>();
        Long selectedId = (long) -1;
        boolean selected = false;
        Object value = field.getValue(issue);
        if (value != null) {
            selected = true;
        }
        for (Option option : options) {
            results.put(option.getOptionId(), option.getValue());
            if (selected && value.toString().equals(option.getValue())) {
                selectedId = option.getOptionId();
            }
        }
        parameters.put("results", results);
        parameters.put("selectedId", selectedId);
        return parameters;
    }
}



