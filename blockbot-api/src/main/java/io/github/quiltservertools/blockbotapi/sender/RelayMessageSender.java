package io.github.quiltservertools.blockbotapi.sender;

public record RelayMessageSender(String name, String nickname, String id, Boolean admin) {
    public String getDisplayName() {
        return nickname != null ? nickname : name;
    }
}
