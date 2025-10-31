-- Create comment_reactions table to track user likes/dislikes
CREATE TABLE comment_reactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reaction_type VARCHAR(10) NOT NULL CHECK (reaction_type IN ('LIKE', 'DISLIKE')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_comment_reactions_comment
        FOREIGN KEY (comment_id)
        REFERENCES task_comments(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_comment_reactions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    -- Unique constraint: one reaction per user per comment
    CONSTRAINT uk_comment_user_reaction UNIQUE (comment_id, user_id)
);

-- Create index for faster lookups
CREATE INDEX idx_comment_reactions_comment_id ON comment_reactions(comment_id);
CREATE INDEX idx_comment_reactions_user_id ON comment_reactions(user_id);

-- Add comments for documentation
COMMENT ON TABLE comment_reactions IS 'Tracks user reactions (likes/dislikes) on task comments';
COMMENT ON COLUMN comment_reactions.reaction_type IS 'Type of reaction: LIKE or DISLIKE';
COMMENT ON COLUMN comment_reactions.created_at IS 'When the reaction was created';
