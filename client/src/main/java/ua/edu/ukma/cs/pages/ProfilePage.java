package ua.edu.ukma.cs.pages;

import ua.edu.ukma.cs.api.response.GameResultStatsResponse;
import ua.edu.ukma.cs.app.PingPongClient;
import ua.edu.ukma.cs.services.GamesResultsStatsService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ProfilePage extends BasePage {

    private final GamesResultsStatsService gamesResultsStatsService;

    private final JLabel usernameLabel;
    private final JLabel totalGamesLabel;
    private final JLabel winsLabel;
    private final JLabel averageScoreLabel;

    public ProfilePage(PingPongClient app, GamesResultsStatsService gamesResultsStatsService) {
        super(app);
        this.gamesResultsStatsService = gamesResultsStatsService;

        usernameLabel = new JLabel("Username: ?");
        totalGamesLabel = new JLabel("Total games: ?");
        winsLabel = new JLabel("Wins: ?");
        averageScoreLabel = new JLabel("Average score: ?");
        JButton toMenuButton = new JButton("Back to main menu");
        toMenuButton.addActionListener(e -> app.showMainMenu());

        JPanel labelsPanel = new JPanel();
        labelsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK),
                        "Profile"
                ),
                BorderFactory.createEmptyBorder(10, 10, 0, 10)
        ));
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));
        for (JComponent label : List.of(usernameLabel, totalGamesLabel, winsLabel, averageScoreLabel)) {
            labelsPanel.add(label);
            labelsPanel.add(Box.createVerticalStrut(10));
        }

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0;
        add(labelsPanel, c);
        c.gridy++;
        add(toMenuButton, c);
    }

    @Override
    public void init() {
        GameResultStatsResponse statsResponse = gamesResultsStatsService.getGameResultsStats();
        usernameLabel.setText("Username: " + app.getAppState().getUsername());
        totalGamesLabel.setText("Total games: " + statsResponse.getTotalGames());
        winsLabel.setText("Wins: " + statsResponse.getWins());
        averageScoreLabel.setText(String.format("Average score: %.2f", statsResponse.getAverageScore()));
    }
}
