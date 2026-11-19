package nicellipse.examples;

import core.common.Position;
import core.elements.*;
import core.exceptions.PositionInvalidException;
import core.movements.*;
import core.sensors.OceanCurrentSensor;
import core.sensors.SalinitySensor;
import core.sensors.TemperatureSensor;
import core.sensors.TurbiditySensor;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

public class BeaconDashboard {
    private static final Color BACKGROUND = new Color(9, 20, 33);
    private static final Color PANEL = new Color(16, 34, 51);
    private static final Color BORDER = new Color(35, 57, 75);
    private static final Color TEXT = new Color(237, 245, 249);
    private static final Color MUTED = new Color(145, 167, 183);
    private static final Color TEAL = new Color(88, 225, 209);
    private static final Color GOLD = new Color(255, 197, 108);
    private static final Color[] BEACON_COLORS = {
            new Color(255, 190, 105),
            new Color(103, 224, 211),
            new Color(140, 180, 255),
            new Color(225, 151, 255),
            new Color(255, 133, 151)
    };

    private final JFrame frame;
    private final Board board;
    private final List<Satellite> satellites;
    private final Map<Beacon, List<Position>> trails = new IdentityHashMap<Beacon, List<Position>>();
    private final Map<Beacon, Color> beaconColors = new IdentityHashMap<Beacon, Color>();
    private final BeaconMap map;
    private final JPanel beaconRows;
    private final JLabel selectedName;
    private final JLabel selectedType;
    private final JLabel selectedPosition;
    private final JLabel selectedCapacity;
    private final JLabel selectedSensors;
    private final JLabel clockLabel;
    private final JLabel statusLabel;
    private final JLabel speedLabel;
    private final JButton playButton;
    private final JSlider speedSlider;
    private final Timer timer;

    private List<Beacon> beacons;
    private Beacon selectedBeacon;
    private double elapsedSeconds;
    private boolean running;

    public BeaconDashboard() {
        this.beacons = createBeacons();
        List<BoardElement> elements = new ArrayList<BoardElement>(beacons);
        this.board = new Board(120, 60, elements, Collections.emptyList());
        this.satellites = createSatellites();
        this.selectedBeacon = beacons.get(0);

        frame = new JFrame("Pelagia | Suivi des balises");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1120, 760));

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 22, 28));
        root.add(createHeader(), BorderLayout.NORTH);

        map = new BeaconMap();
        beaconRows = new JPanel();
        beaconRows.setOpaque(false);
        beaconRows.setLayout(new BoxLayout(beaconRows, BoxLayout.Y_AXIS));
        selectedName = valueLabel("B-01");
        selectedType = valueLabel("Déplacement horizontal");
        selectedPosition = valueLabel("12.0 · 12.0");
        selectedCapacity = valueLabel("100 unités");
        selectedSensors = valueLabel("Température, Courant");
        JPanel sidePanel = createSidePanel();

        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setOpaque(false);
        content.add(map, BorderLayout.CENTER);
        content.add(sidePanel, BorderLayout.EAST);
        root.add(content, BorderLayout.CENTER);

        clockLabel = valueLabel("00:00");
        statusLabel = valueLabel("Simulation active");
        speedLabel = valueLabel("1,0×");
        playButton = new JButton("Pause");
        speedSlider = new JSlider(1, 30, 10);
        root.add(createControls(), BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.setSize(1380, 860);
        frame.setLocationRelativeTo(null);

        timer = new Timer(60, event -> advanceSimulation());
        timer.setCoalesce(true);
        initializeTrailsAndColors();
        refreshBeacons();
        updateSelection();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 12));
        header.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel eyebrow = label("PELAGIA  /  RÉSEAU OCÉANIQUE", 11, new Color(113, 197, 199), true);
        JLabel title = label("Suivi des balises", 28, TEXT, true);
        JLabel subtitle = label("Visualisez les trajectoires et les positions en temps réel.", 13, MUTED, false);
        titleBlock.add(eyebrow);
        titleBlock.add(Box.createVerticalStrut(8));
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(5));
        titleBlock.add(subtitle);

        JPanel status = new JPanel(new BorderLayout(9, 0));
        status.setOpaque(false);
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel dot = label("●", 12, TEAL, true);
        JLabel online = label("LIAISON SATELLITE", 10, TEXT, true);
        status.add(dot, BorderLayout.WEST);
        status.add(online, BorderLayout.CENTER);
        status.add(label("EN LIGNE", 9, TEAL, true), BorderLayout.SOUTH);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);
        header.add(createMetricStrip(), BorderLayout.SOUTH);
        return header;
    }

    private JPanel createMetricStrip() {
        JPanel strip = new JPanel(new BorderLayout(1, 0));
        strip.setOpaque(false);
        strip.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        strip.add(metric("05", "BALISES DÉPLOYÉES", GOLD), BorderLayout.WEST);
        strip.add(metric("04", "EN MOUVEMENT", TEAL), BorderLayout.CENTER);
        strip.add(metric("02", "SATELLITES ACTIFS", new Color(151, 184, 255)), BorderLayout.EAST);
        return strip;
    }

    private JPanel metric(String value, String caption, Color color) {
        JPanel metric = new JPanel(new BorderLayout(10, 0));
        metric.setOpaque(false);
        metric.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(10, 0, 0, 0)));
        metric.add(label(value, 18, color, true), BorderLayout.WEST);
        metric.add(label(caption, 10, MUTED, true), BorderLayout.CENTER);
        return metric;
    }

    private JPanel createSidePanel() {
        JPanel side = new JPanel();
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(278, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        JPanel listCard = new CardPanel(PANEL, BORDER);
        listCard.setLayout(new BorderLayout(0, 10));
        listCard.setBorder(BorderFactory.createEmptyBorder(17, 16, 15, 16));
        listCard.add(label("BALISES", 11, MUTED, true), BorderLayout.NORTH);
        listCard.add(beaconRows, BorderLayout.CENTER);
        side.add(listCard);
        side.add(Box.createVerticalStrut(14));

        JPanel detailCard = new CardPanel(PANEL, BORDER);
        detailCard.setLayout(new BoxLayout(detailCard, BoxLayout.Y_AXIS));
        detailCard.setBorder(BorderFactory.createEmptyBorder(17, 16, 17, 16));
        detailCard.add(label("BALISE SÉLECTIONNÉE", 10, MUTED, true));
        detailCard.add(Box.createVerticalStrut(10));
        selectedName.setFont(new Font("SansSerif", Font.BOLD, 19));
        detailCard.add(selectedName);
        detailCard.add(Box.createVerticalStrut(4));
        detailCard.add(selectedType);
        detailCard.add(Box.createVerticalStrut(16));
        detailCard.add(detailRow("POSITION  X · Y", selectedPosition));
        detailCard.add(Box.createVerticalStrut(11));
        detailCard.add(detailRow("CAPACITÉ", selectedCapacity));
        detailCard.add(Box.createVerticalStrut(11));
        detailCard.add(detailRow("CAPTEURS", selectedSensors));
        detailCard.add(Box.createVerticalStrut(17));
        detailCard.add(label("ÉTAT", 9, MUTED, true));
        detailCard.add(Box.createVerticalStrut(6));
        detailCard.add(label("●  Collecte des mesures", 12, TEAL, false));
        side.add(detailCard);
        side.add(Box.createVerticalGlue());
        return side;
    }

    private JPanel detailRow(String caption, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(0, 5));
        row.setOpaque(false);
        row.add(label(caption, 9, MUTED, true), BorderLayout.NORTH);
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    private JPanel createControls() {
        JPanel controls = new CardPanel(PANEL, BORDER);
        controls.setLayout(new BorderLayout(16, 0));
        controls.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        controls.setPreferredSize(new Dimension(0, 68));

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        styleButton(playButton, true);
        JButton resetButton = new JButton("Réinitialiser");
        styleButton(resetButton, false);
        playButton.addActionListener(event -> togglePlayback());
        resetButton.addActionListener(event -> resetSimulation());
        buttons.add(playButton);
        buttons.add(Box.createHorizontalStrut(8));
        buttons.add(resetButton);

        JPanel speed = new JPanel(new BorderLayout(10, 0));
        speed.setOpaque(false);
        speed.add(label("VITESSE", 9, MUTED, true), BorderLayout.WEST);
        speedSlider.setOpaque(false);
        speedSlider.setFocusable(false);
        speedSlider.setPreferredSize(new Dimension(160, 28));
        speedSlider.addChangeListener(event -> {
            double multiplier = speedSlider.getValue() / 10.0;
            speedLabel.setText(String.format(java.util.Locale.FRANCE, "%.1f×", multiplier));
        });
        speed.add(speedSlider, BorderLayout.CENTER);
        speed.add(speedLabel, BorderLayout.EAST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        clockLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        info.add(clockLabel);
        info.add(Box.createVerticalStrut(3));
        info.add(statusLabel);

        controls.add(buttons, BorderLayout.WEST);
        controls.add(speed, BorderLayout.CENTER);
        controls.add(info, BorderLayout.EAST);
        return controls;
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(primary ? BACKGROUND : TEXT);
        button.setBackground(primary ? TEAL : new Color(27, 47, 65));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 15, 9, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void refreshBeacons() {
        beaconRows.removeAll();
        for (int i = 0; i < beacons.size(); i++) {
            Beacon beacon = beacons.get(i);
            Color color = beaconColors.get(beacon);
            String movement = movementName(beacon.getMovementStrategy());
            JButton row = new JButton("<html><div style='padding:3px 1px'><b>"
                    + beacon.getLabel() + "</b><br><span style='color:#91a7b7'>"
                    + movement + "</span></div></html>");
            row.setHorizontalAlignment(SwingConstants.LEFT);
            row.setForeground(TEXT);
            row.setFont(new Font("SansSerif", Font.PLAIN, 12));
            row.setBackground(beacon == selectedBeacon ? new Color(30, 58, 72) : PANEL);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, color),
                    BorderFactory.createEmptyBorder(7, 10, 7, 6)));
            row.setFocusPainted(false);
            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            row.addActionListener(event -> {
                selectedBeacon = beacon;
                refreshBeacons();
                updateSelection();
                map.repaint();
            });
            row.setAlignmentX(JPanel.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 57));
            beaconRows.add(row);
            if (i < beacons.size() - 1) {
                beaconRows.add(Box.createVerticalStrut(3));
            }
        }
        beaconRows.revalidate();
        beaconRows.repaint();
    }

    private void updateSelection() {
        if (selectedBeacon == null) {
            return;
        }
        selectedName.setText(selectedBeacon.getLabel());
        selectedName.setForeground(beaconColors.get(selectedBeacon));
        selectedType.setText(movementName(selectedBeacon.getMovementStrategy()));
        Position position = selectedBeacon.getPosition();
        selectedPosition.setText(String.format(java.util.Locale.FRANCE, "%.1f · %.1f",
                position.getX(), position.getY()));
        selectedCapacity.setText(selectedBeacon.getCapacity() + " unités");
        StringBuilder sensorNames = new StringBuilder();
        for (Sensor sensor : selectedBeacon.getSensors()) {
            if (sensorNames.length() > 0) {
                sensorNames.append(", ");
            }
            sensorNames.append(sensor.getLabel());
        }
        selectedSensors.setText(sensorNames.length() == 0 ? "Aucun" : sensorNames.toString());
    }

    private void advanceSimulation() {
        double step = 0.06 * speedSlider.getValue() / 10.0;
        try {
            for (Beacon beacon : beacons) {
                Random random = new Random();
                if (random.nextInt(250) == 0) {
                    beacon.setSpeed(-beacon.getSpeed());
                }
                beacon.move(step, board);
                List<Position> trail = trails.get(beacon);
                Position position = beacon.getPosition();
                trail.add(new Position(position.getX(), position.getY()));
                if (trail.size() > 90) {
                    trail.remove(0);
                }
            }
            elapsedSeconds += step;
            clockLabel.setText(formatTime(elapsedSeconds));
            updateSelection();
            map.repaint();
        } catch (PositionInvalidException exception) {
            timer.stop();
            running = false;
            playButton.setText("Reprendre");
            statusLabel.setText("Limite du plateau atteinte");
            statusLabel.setForeground(GOLD);
            map.repaint();
        }
    }

    private void togglePlayback() {
        if (running) {
            timer.stop();
            running = false;
            playButton.setText("Reprendre");
            statusLabel.setText("Simulation en pause");
            statusLabel.setForeground(MUTED);
        } else {
            timer.start();
            running = true;
            playButton.setText("Pause");
            statusLabel.setText("Simulation active");
            statusLabel.setForeground(TEAL);
        }
    }

    private void resetSimulation() {
        timer.stop();
        running = false;
        beacons = createBeacons();
        board.setBoardElements(new ArrayList<BoardElement>(beacons));
        elapsedSeconds = 0;
        selectedBeacon = beacons.get(0);
        initializeTrailsAndColors();
        clockLabel.setText("00:00");
        statusLabel.setText("Simulation réinitialisée");
        statusLabel.setForeground(MUTED);
        playButton.setText("Démarrer");
        refreshBeacons();
        updateSelection();
        map.repaint();
    }

    private void initializeTrailsAndColors() {
        trails.clear();
        beaconColors.clear();
        for (int i = 0; i < beacons.size(); i++) {
            Beacon beacon = beacons.get(i);
            List<Position> trail = new ArrayList<Position>();
            Position position = beacon.getPosition();
            trail.add(new Position(position.getX(), position.getY()));
            trails.put(beacon, trail);
            beaconColors.put(beacon, BEACON_COLORS[i % BEACON_COLORS.length]);
        }
    }

    private List<Beacon> createBeacons() {
        List<Beacon> result = new ArrayList<Beacon>();
        result.add(new Beacon("B-01", new Position(13, 12), new HorizontalMovement(),
                1.8, sensors(new TemperatureSensor("Température"), new OceanCurrentSensor("Courant"))));
        result.add(new Beacon("B-02", new Position(36, 27), new SinusoidalMovement(4.0, 0.12),
                1.35, sensors(new TemperatureSensor("Température"), new SalinitySensor("Salinité"))));
        result.add(new Beacon("B-03", new Position(65, 43), new VerticalMovement(),
                -0.34, sensors(new TurbiditySensor("Turbidité"), new OceanCurrentSensor("Courant"))));
        result.add(new Beacon("B-04", new Position(87, 18), new StationaryMovement(),
                0, sensors(new SalinitySensor("Salinité"))));
        result.add(new Beacon("B-05", new Position(109, 35), new HorizontalMovement(),
                -1.5, sensors(new TemperatureSensor("Température"), new TurbiditySensor("Turbidité"))));
        return result;
    }

    private List<Sensor> sensors(Sensor... sensors) {
        List<Sensor> result = new ArrayList<Sensor>();
        Collections.addAll(result, sensors);
        return result;
    }

    private List<Satellite> createSatellites() {
        List<Satellite> result = new ArrayList<Satellite>();
        result.add(new Satellite("SAT-01", new Position(24, 0), new StationaryMovement(), 0, true));
        result.add(new Satellite("SAT-02", new Position(82, 0), new StationaryMovement(), 0, true));
        return result;
    }

    private String movementName(MovementStrategy strategy) {
        if (strategy instanceof HorizontalMovement) {
            return "Déplacement horizontal";
        }
        if (strategy instanceof VerticalMovement) {
            return "Déplacement vertical";
        }
        if (strategy instanceof SinusoidalMovement) {
            return "Trajectoire sinusoïdale";
        }
        return "Position stationnaire";
    }

    private String formatTime(double seconds) {
        int total = (int) seconds;
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    private JLabel label(String text, int size, Color color, boolean bold) {
        JLabel result = new JLabel(text);
        result.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
        result.setForeground(color);
        return result;
    }

    private JLabel valueLabel(String text) {
        return label(text, 12, TEXT, false);
    }

    public void show() {
        frame.setVisible(true);
        timer.start();
        running = true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BeaconDashboard().show());
    }

    private class BeaconMap extends JPanel {
        private static final int HORIZONTAL_PADDING = 24;
        private static final int VERTICAL_PADDING = 22;

        private BeaconMap() {
            setOpaque(false);
            setPreferredSize(new Dimension(780, 500));
            setMinimumSize(new Dimension(560, 420));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    Beacon nearest = null;
                    double nearestDistance = 24;
                    for (Beacon beacon : beacons) {
                        Position position = beacon.getPosition();
                        double dx = event.getX() - screenX(position.getX());
                        double dy = event.getY() - screenY(position.getY());
                        double distance = Math.sqrt(dx * dx + dy * dy);
                        if (distance < nearestDistance) {
                            nearest = beacon;
                            nearestDistance = distance;
                        }
                    }
                    if (nearest != null) {
                        selectedBeacon = nearest;
                        refreshBeacons();
                        updateSelection();
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            g.setColor(PANEL);
            g.fill(new RoundRectangle2D.Double(0, 0, width - 1, height - 1, 18, 18));
            g.clip(new RoundRectangle2D.Double(0, 0, width, height, 18, 18));

            int left = HORIZONTAL_PADDING;
            int right = width - HORIZONTAL_PADDING;
            int top = VERTICAL_PADDING;
            int bottom = height - VERTICAL_PADDING;
            int surface = top + (int) ((bottom - top) * 0.29);

            g.setPaint(new GradientPaint(0, top, new Color(225, 241, 239),
                    0, surface, new Color(246, 250, 244)));
            g.fillRect(0, 0, width, surface + 1);
            g.setPaint(new GradientPaint(0, surface, new Color(40, 165, 176),
                    0, bottom, new Color(10, 40, 62)));
            g.fillRect(0, surface, width, height - surface);

            drawSky(g, left, right, top, surface);
            drawOcean(g, left, right, surface, bottom);
            drawSatellites(g, left, right, top, surface);
            drawBeacons(g);
            drawMapLabels(g, left, right, surface, bottom);

            g.setClip(null);
            g.setColor(BORDER);
            g.setStroke(new BasicStroke(1f));
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1, 18, 18));
            g.dispose();
        }

        private void drawSky(Graphics2D g, int left, int right, int top, int surface) {
            g.setColor(new Color(32, 75, 96, 25));
            g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                    1, new float[]{5, 7}, 0));
            g.draw(new Ellipse2D.Double(left + 15, top - 49, right - left - 30, surface + 82));
            g.setColor(new Color(255, 255, 255, 135));
            for (int i = 0; i < 16; i++) {
                int x = left + 10 + (i * 73) % Math.max(1, right - left - 20);
                int y = top + 8 + (i * 31) % Math.max(1, surface - top - 12);
                g.fill(new Ellipse2D.Double(x, y, 2, 2));
            }
            g.setColor(new Color(97, 132, 140, 130));
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.drawString("ORBITE", left + 2, top + 11);
        }

        private void drawOcean(Graphics2D g, int left, int right, int surface, int bottom) {
            g.setColor(new Color(224, 255, 248, 105));
            g.setStroke(new BasicStroke(1.2f));
            Path2D wave = new Path2D.Double();
            wave.moveTo(0, surface);
            for (int x = 0; x <= getWidth(); x += 12) {
                wave.lineTo(x, surface + 2 * Math.sin(x * 0.035) + Math.sin(x * 0.011) * 2);
            }
            g.draw(wave);

            g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                    1, new float[]{3, 6}, 0));
            g.setColor(new Color(204, 243, 244, 45));
            for (int i = 1; i < 5; i++) {
                int y = surface + (bottom - surface) * i / 5;
                g.draw(new Line2D.Double(left, y, right, y));
            }
            for (int i = 0; i < 7; i++) {
                int x = left + (right - left) * i / 6;
                g.draw(new Line2D.Double(x, surface + 12, x, bottom));
            }

            g.setColor(new Color(7, 29, 47, 105));
            Path2D seabed = new Path2D.Double();
            seabed.moveTo(0, bottom);
            seabed.lineTo(0, bottom - 14);
            for (int x = 0; x <= getWidth(); x += 20) {
                seabed.lineTo(x, bottom - 14 + 5 * Math.sin(x * 0.025));
            }
            seabed.lineTo(getWidth(), getHeight());
            seabed.closePath();
            g.fill(seabed);
        }

        private void drawSatellites(Graphics2D g, int left, int right, int top, int surface) {
            for (int i = 0; i < satellites.size(); i++) {
                Satellite satellite = satellites.get(i);
                Position position = satellite.getPosition();
                int x = left + (int) (position.getX() / board.getWidth() * (right - left));
                int y = top + 30 + i * Math.max(24, (surface - top - 55) / 2);
                drawSatellite(g, x, y);
                g.setFont(new Font("SansSerif", Font.BOLD, 9));
                g.setColor(new Color(74, 106, 113));
                g.drawString(satellite.getLabel(), x + 23, y + 3);
            }
        }

        private void drawSatellite(Graphics2D g, int x, int y) {
            Graphics2D satellite = (Graphics2D) g.create();
            satellite.translate(x, y);
            satellite.rotate(-Math.PI / 4);
            satellite.setColor(new Color(53, 88, 111));
            satellite.fillRoundRect(-26, -5, 16, 10, 3, 3);
            satellite.fillRoundRect(10, -5, 16, 10, 3, 3);
            satellite.setColor(new Color(110, 163, 183));
            for (int i = -23; i < -12; i += 5) {
                satellite.drawLine(i, -4, i, 4);
            }
            for (int i = 13; i < 24; i += 5) {
                satellite.drawLine(i, -4, i, 4);
            }
            satellite.setColor(new Color(47, 83, 106));
            satellite.fillRoundRect(-8, -8, 16, 16, 5, 5);
            satellite.setColor(new Color(236, 177, 95));
            satellite.fill(new Ellipse2D.Double(4, 5, 4, 4));
            satellite.setColor(new Color(240, 249, 246, 220));
            satellite.setStroke(new BasicStroke(1.2f));
            satellite.drawRoundRect(-8, -8, 16, 16, 5, 5);
            satellite.dispose();
        }

        private void drawBeacons(Graphics2D g) {
            for (Beacon beacon : beacons) {
                List<Position> trail = trails.get(beacon);
                Color color = beaconColors.get(beacon);
                if (trail != null && trail.size() > 1) {
                    for (int i = 1; i < trail.size(); i++) {
                        float alpha = 0.08f + 0.60f * i / trail.size();
                        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * alpha)));
                        g.setStroke(new BasicStroke(2.0f));
                        Position previous = trail.get(i - 1);
                        Position current = trail.get(i);
                        g.draw(new Line2D.Double(screenX(previous.getX()), screenY(previous.getY()),
                                screenX(current.getX()), screenY(current.getY())));
                    }
                }
                drawBeacon(g, beacon, color);
            }
        }

        private void drawBeacon(Graphics2D g, Beacon beacon, Color color) {
            Position position = beacon.getPosition();
            int x = screenX(position.getX());
            int y = screenY(position.getY());
            int surface = VERTICAL_PADDING + (int) ((getHeight() - VERTICAL_PADDING * 2) * 0.29);

            g.setColor(new Color(240, 255, 244, 80));
            g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                    1, new float[]{3, 5}, 0));
            g.draw(new Line2D.Double(x, surface + 5, x, y - 15));

            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 42));
            g.fill(new Ellipse2D.Double(x - 19, y - 19, 38, 38));
            g.setColor(new Color(8, 35, 51, 230));
            g.fill(new Ellipse2D.Double(x - 11, y - 11, 22, 22));
            g.setColor(color);
            g.setStroke(new BasicStroke(2.2f));
            g.draw(new Ellipse2D.Double(x - 10, y - 10, 20, 20));
            g.fill(new Ellipse2D.Double(x - 3, y - 3, 6, 6));
            g.setColor(new Color(245, 250, 245));
            g.fill(new Ellipse2D.Double(x - 6, y - 1, 3, 3));
            g.fill(new Ellipse2D.Double(x + 3, y - 1, 3, 3));

            if (beacon == selectedBeacon) {
                g.setColor(new Color(255, 255, 255, 210));
                g.setStroke(new BasicStroke(1.3f));
                g.draw(new Ellipse2D.Double(x - 15, y - 15, 30, 30));
            }

            String name = beacon.getLabel();
            int textWidth = g.getFontMetrics(new Font("SansSerif", Font.BOLD, 10)).stringWidth(name);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.setColor(new Color(8, 26, 40, 205));
            g.fillRoundRect(x - textWidth / 2 - 7, y + 16, textWidth + 14, 20, 8, 8);
            g.setColor(new Color(236, 244, 246));
            g.drawString(name, x - textWidth / 2, y + 30);
        }

        private void drawMapLabels(Graphics2D g, int left, int right, int surface, int bottom) {
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.setColor(new Color(237, 255, 247, 185));
            g.drawString("SURFACE", left + 2, surface + 17);
            g.setColor(new Color(226, 246, 247, 150));
            g.drawString("0 m", left + 2, surface + 33);
            for (int i = 1; i < 5; i++) {
                int y = surface + (bottom - surface) * i / 5;
                g.drawString((i * 20) + " m", left + 2, y - 5);
            }
            g.setColor(new Color(228, 244, 240, 170));
            g.drawString("ZONE OCÉANIQUE", right - 112, surface + 20);
        }

        private int screenX(double x) {
            int usableWidth = getWidth() - HORIZONTAL_PADDING * 2;
            return HORIZONTAL_PADDING + (int) Math.round(x / board.getWidth() * usableWidth);
        }

        private int screenY(double y) {
            int top = VERTICAL_PADDING + (int) ((getHeight() - VERTICAL_PADDING * 2) * 0.29) + 32;
            int bottom = getHeight() - VERTICAL_PADDING - 28;
            return top + (int) Math.round(y / board.getHeight() * (bottom - top));
        }
    }

    private static class CardPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Color fill;
        private final Color outline;

        private CardPanel(Color fill, Color outline) {
            this.fill = fill;
            this.outline = outline;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
            g.setColor(outline);
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2, 16, 16));
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
