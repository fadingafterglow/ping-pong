package ua.edu.ukma.cs.pages;

import ua.edu.ukma.cs.api.request.GameResultFilterDto;
import ua.edu.ukma.cs.api.response.GameResultListResponse;
import ua.edu.ukma.cs.api.response.GameResultResponse;
import ua.edu.ukma.cs.app.PingPongClient;
import ua.edu.ukma.cs.services.GamesResultsService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

public class GamesResultsPage extends BasePage {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final GamesResultsService gamesResultsService;

    private final JPanel resultsPanel;

    private final JButton prevButton;
    private final JButton nextButton;

    private final JLabel totalLabel;
    private final JLabel pageLabel;

    private GameResultFilterDto filter;
    private int page;

    public GamesResultsPage(PingPongClient app, GamesResultsService gamesResultsService) {
        super(app);
        this.gamesResultsService = gamesResultsService;

        JButton toMenuButton = new JButton("Back to main menu");
        toMenuButton.addActionListener(e -> app.showMainMenu());
        JButton filterButton = new JButton("Change filter");
        filterButton.addActionListener(this::changeFilter);
        totalLabel = new JLabel("Total: 0");
        prevButton = new JButton("Previous");
        prevButton.addActionListener(this::previousPage);
        nextButton = new JButton("Next");
        nextButton.addActionListener(this::nextPage);
        pageLabel = new JLabel("Page 0 of 0");

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(toMenuButton);
        topPanel.add(filterButton);
        topPanel.add(totalLabel);
        topPanel.add(prevButton);
        topPanel.add(pageLabel);
        topPanel.add(nextButton);

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void init() {
        page = 0;
        filter = GameResultFilterDto.builder().size(10).sortBy("timeFinished").descendingOrder(true).build();
        applyFilter();
    }

    private void applyFilter() {
        filter.setPage(page);
        GameResultListResponse gameResults = gamesResultsService.getGameResultsByFilter(filter);

        int totalPages;
        if (gameResults.getTotal() == 0) {
            page = -1;
            totalPages = 0;
        }
        else if (filter.getSize() <= 0) {
            page = 0;
            totalPages = 1;
        } else
            totalPages = (int) ((gameResults.getTotal() - 1) / filter.getSize() + 1);

        totalLabel.setText(String.format("Total: %d", gameResults.getTotal()));
        pageLabel.setText(String.format("Page %d of %d", page + 1, totalPages));

        prevButton.setEnabled(page > 0);
        nextButton.setEnabled(page < totalPages - 1);

        resultsPanel.removeAll();
        gameResults.getItems().forEach(this::addResultPanel);

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void changeFilter(ActionEvent e) {
        GamesResultsFilterDialog dialog = new GamesResultsFilterDialog(app);
        filter = dialog.showDialog();
        page = 0;
        applyFilter();
    }

    private void previousPage(ActionEvent e) {
        page--;
        applyFilter();
    }

    private void nextPage(ActionEvent e) {
        page++;
        applyFilter();
    }

    private void addResultPanel(GameResultResponse result) {
        JPanel resultPanel = createGameResultPanel(result);
        resultsPanel.add(resultPanel);
        resultsPanel.add(Box.createVerticalStrut(10));
    }

    private JPanel createGameResultPanel(GameResultResponse result) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        String scoreLine = String.format("%s (%d) VS %s (%d)",
                result.getCreatorUsername(), result.getCreatorScore(),
                result.getOtherUsername(), result.getOtherScore());
        JLabel scoreLabel = new JLabel(scoreLine);

        JLabel timeLabel = new JLabel("Finished: " + convertDateTime(result.getTimeFinished()));

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(scoreLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(timeLabel);

        return panel;
    }

    private String convertDateTime(LocalDateTime utcDateTime) {
        ZonedDateTime zonedUtcDateTime = utcDateTime.atZone(ZoneOffset.UTC);
        ZonedDateTime localDateTime = zonedUtcDateTime.withZoneSameInstant(ZoneId.systemDefault());
        return localDateTime.format(FORMATTER);
    }

    private static class GamesResultsFilterDialog extends JDialog {

        private final JTextField usernameField = new JTextField();
        private final JTextField minThisUserScoreField = new JTextField();
        private final JTextField maxThisUserScoreField = new JTextField();
        private final JTextField minOtherUserScoreField = new JTextField();
        private final JTextField maxOtherUserScoreField = new JTextField();

        private final JSpinner minTimeFinishedSpinner;
        private final JSpinner maxTimeFinishedSpinner;

        private final JTextField sizeField = new JTextField("10");

        private final GameResultFilterDto.GameResultFilterDtoBuilder resultBuilder = GameResultFilterDto.builder()
                .size(10)
                .sortBy("timeFinished")
                .descendingOrder(true);

        public GamesResultsFilterDialog(Frame parent) {
            super(parent, "Set Filters", true);
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(5, 10, 5, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0;
            add(new JLabel("Username:"), gbc);
            gbc.gridx = 1;
            add(usernameField, gbc);

            gbc.gridx = 0; gbc.gridy++;
            add(new JLabel("Yours min score:"), gbc);
            gbc.gridx = 1;
            add(minThisUserScoreField, gbc);

            gbc.gridx = 0; gbc.gridy++;
            add(new JLabel("Yours max score:"), gbc);
            gbc.gridx = 1;
            add(maxThisUserScoreField, gbc);

            gbc.gridx = 0; gbc.gridy++;
            add(new JLabel("Opponent's min score:"), gbc);
            gbc.gridx = 1;
            add(minOtherUserScoreField, gbc);

            gbc.gridx = 0; gbc.gridy++;
            add(new JLabel("Opponent's max score:"), gbc);
            gbc.gridx = 1;
            add(maxOtherUserScoreField, gbc);

            gbc.gridx = 0; gbc.gridy++;
            add(new JLabel("Min time finished:"), gbc);
            minTimeFinishedSpinner = new JSpinner(new SpinnerDateModel());
            minTimeFinishedSpinner.setValue(Date.from(LocalDateTime.now().minusYears(1).atZone(ZoneId.systemDefault()).toInstant()));
            gbc.gridx = 1;
            add(minTimeFinishedSpinner, gbc);

            gbc.gridx = 0; gbc.gridy++;
            add(new JLabel("Max time finished:"), gbc);
            maxTimeFinishedSpinner = new JSpinner(new SpinnerDateModel());
            maxTimeFinishedSpinner.setValue(Date.from(LocalDateTime.now().plusYears(1).atZone(ZoneId.systemDefault()).toInstant()));
            gbc.gridx = 1;
            add(maxTimeFinishedSpinner, gbc);

            JSpinner.DateEditor editor1 = new JSpinner.DateEditor(minTimeFinishedSpinner, "yyyy-MM-dd HH:mm");
            JSpinner.DateEditor editor2 = new JSpinner.DateEditor(maxTimeFinishedSpinner, "yyyy-MM-dd HH:mm");
            minTimeFinishedSpinner.setEditor(editor1);
            maxTimeFinishedSpinner.setEditor(editor2);

            gbc.gridx = 0; gbc.gridy++;
            add(new JLabel("Page size:"), gbc);
            gbc.gridx = 1;
            add(sizeField, gbc);

            gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy++;
            JButton okButton = new JButton("Apply");
            add(okButton, gbc);

            okButton.addActionListener(e -> {
                resultBuilder
                        .username(usernameField.getText())
                        .minThisUserScore(parseInt(minThisUserScoreField.getText()))
                        .maxThisUserScore(parseInt(maxThisUserScoreField.getText()))
                        .minOtherUserScore(parseInt(minOtherUserScoreField.getText()))
                        .maxOtherUserScore(parseInt(maxOtherUserScoreField.getText()))
                        .minTimeFinished(toDateTime(minTimeFinishedSpinner.getValue()))
                        .maxTimeFinished(toDateTime(maxTimeFinishedSpinner.getValue()))
                        .size(Optional.ofNullable(parseInt(sizeField.getText())).orElse(10));
                dispose();
            });

            pack();
            setLocationRelativeTo(parent);
        }

        private Integer parseInt(String s) {
            try {
                return s.isBlank() ? null : Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private LocalDateTime toDateTime(Object value) {
            if (value instanceof Date d)
                return d.toInstant().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
            return null;
        }

        public GameResultFilterDto showDialog() {
            setVisible(true);
            return resultBuilder.build();
        }
    }
}
