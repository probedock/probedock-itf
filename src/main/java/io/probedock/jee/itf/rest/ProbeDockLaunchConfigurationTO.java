package io.probedock.jee.itf.rest;

public class ProbeDockLaunchConfigurationTO extends LaunchConfigurationTO {
    private String category;
    private String projectApiId;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean hasCategory() {
        return category != null && !category.isEmpty();
    }

    public String getProjectApiId() {
        return projectApiId;
    }

    public void setProjectApiId(String projectApiId) {
        this.projectApiId = projectApiId;
    }

    @Override
    public String toString() {
        StringBuilder message = new StringBuilder(super.toString().replaceAll("\\}", ""));

        if (hasCategory()) {
            message.append(", Category: ").append(category);
        }

        if (projectApiId != null && !projectApiId.isEmpty()) {
            message.append(", Project API ID: ").append(projectApiId);
        }

        return message.append("}").toString();
    }
}
