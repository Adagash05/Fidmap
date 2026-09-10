package com.amsal.fidmap.workspace;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class WorkspaceSlugGenerator {

    public String generate(String workspaceName) {

        if (workspaceName == null || workspaceName.isBlank()) {
            throw new IllegalArgumentException(
                    "Workspace name cannot be empty"
            );
        }

        String slug = Normalizer
                .normalize(workspaceName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        if (slug.isBlank()) {
            throw new IllegalArgumentException(
                    "Unable to generate a valid workspace slug"
            );
        }

        return slug;
    }
}