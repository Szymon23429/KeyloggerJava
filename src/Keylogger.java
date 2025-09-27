import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class Keylogger extends JFrame implements KeyListener {
    private JTextArea pole;
    private JButton Start;
    private JButton Stop;
    private JCheckBox zgoda;
    private BufferedWriter pisarz;
    private boolean logowanieAktywne = false;
    private JLabel statystyki;
    private long Czas;
    public Keylogger() {
        super("Keylogger");
        setSize(720, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        pole = new JTextArea();
        pole.setLineWrap(true);
        pole.setWrapStyleWord(true);
        pole.addKeyListener(this);
        JScrollPane przewijanie = new JScrollPane(pole);

        Start = new JButton("Start");
        Stop = new JButton("Stop");
        Stop.setEnabled(false);

        zgoda = new JCheckBox("Wyrażam zgodę");

        statystyki = new JLabel("Znaki: 0 | Słowa: 0 | Czas: 0s");

        JButton przyciskCSV = new JButton("Eksportuj do CSV");
        przyciskCSV.addActionListener(e -> eksportCSV());

        JButton przyciskHTML = new JButton("Eksportuj do HTML");
        przyciskHTML.addActionListener(e -> eksportHTML());

        Start.addActionListener(e -> rozpocznij());
        Stop.addActionListener(e -> zatrzymaj());

        JPanel panelDolny = new JPanel();
        panelDolny.add(zgoda);
        panelDolny.add(Start);
        panelDolny.add(Stop);
        panelDolny.add(statystyki);
        panelDolny.add(przyciskCSV);
        panelDolny.add(przyciskHTML);

        add(przewijanie, BorderLayout.CENTER);
        add(panelDolny, BorderLayout.SOUTH);

        getContentPane().setBackground(Color.DARK_GRAY);
        pole.setBackground(new Color(30, 30, 30));
        pole.setForeground(Color.WHITE);
        pole.setCaretColor(Color.WHITE);

        Start.setBackground(Color.GRAY);
        Start.setForeground(Color.WHITE);

        Stop.setBackground(Color.GRAY);
        Stop.setForeground(Color.WHITE);

        zgoda.setBackground(Color.DARK_GRAY);
        zgoda.setForeground(Color.WHITE);

        panelDolny.setBackground(Color.DARK_GRAY);
        panelDolny.setForeground(Color.DARK_GRAY);

        statystyki.setBackground(Color.DARK_GRAY);
        statystyki.setForeground(Color.WHITE);

    }
    private void rozpocznij() {
        if (!zgoda.isSelected()) {
            JOptionPane.showMessageDialog(this, "Zgoda", "Brak zgody", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Wybierz plik");

            int wynik = chooser.showSaveDialog(this);
            if (wynik != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File plik = chooser.getSelectedFile();
            pisarz = new BufferedWriter(new FileWriter(plik, true));

             Zapis("\nData " + aktualnaData());
            logowanieAktywne = true;
            Czas = System.currentTimeMillis();
            Start.setEnabled(false);
            Stop.setEnabled(true);
            pole.requestFocusInWindow();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Błąd zapisu", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void zatrzymaj() {
        try {
            if (logowanieAktywne) {
                Zapis("Koniec " + aktualnaData());
                pisarz.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        logowanieAktywne = false;
        Start.setEnabled(true);
        Stop.setEnabled(false);
    }
    private void Zapis(String tekst) throws IOException {
        if (pisarz != null) {
            pisarz.write(tekst);
            pisarz.newLine();
            pisarz.flush();
        }
    }
    private String aktualnaData() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    private void aktualizujStatystyki() {
        String tekst = pole.getText();
        int znaki = tekst.length();
        int slowa = tekst.trim().isEmpty() ? 0 : tekst.trim().split("\\s+").length;
        long czasSekundy = (System.currentTimeMillis() - Czas) / 1000;

        statystyki.setText("Znaki: " + znaki + " | Słowa: " + slowa + " | Czas: " + czasSekundy + "s");
    }
    @Override
    public void keyTyped(KeyEvent e) {
        if (logowanieAktywne) {
            try {
                char znak = e.getKeyChar();
                if (znak == '\b') {
                    Zapis("BACKSPACE");
                } else if (znak == '\n') {
                   Zapis("ENTER");
                } else {
                    Zapis("Znak: " + znak);
                }
                aktualizujStatystyki();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    @Override
    public void keyPressed(KeyEvent e) {
    }
    @Override
    public void keyReleased(KeyEvent e) {
    }

    private void eksportCSV() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Zapisz jako CSV");
            int wynik = chooser.showSaveDialog(this);
            if (wynik != JFileChooser.APPROVE_OPTION) return;

            File plik = chooser.getSelectedFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(plik));

            String[] linie = pole.getText().split("\n");
            for (String linia : linie) {
                writer.write("\"" + linia.replace("\"", "\"\"") + "\"");
                writer.newLine();
            }

            writer.close();
            JOptionPane.showMessageDialog(this, "Eksport zakończony");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void eksportHTML() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Zapisz jako HTML");
            int wynik = chooser.showSaveDialog(this);
            if (wynik != JFileChooser.APPROVE_OPTION) return;

            File plik = chooser.getSelectedFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(plik));

            writer.write("<html><head><title>Log</title></head><body>");
            writer.write("<pre>");
            writer.write(pole.getText().replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;"));
            writer.write("</pre>");
            writer.write("</body></html>");
            writer.close();
            JOptionPane.showMessageDialog(this, "Eksport zakończony");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Keylogger().setVisible(true);
        });
    }
}
