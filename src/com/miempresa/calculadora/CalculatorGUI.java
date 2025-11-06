package com.miempresa.calculadora;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class CalculatorGUI extends JFrame {

    private final JTextField display;
    private StringBuilder currentExpression;
    private final ResultDAO dao;

    public CalculatorGUI() {
        super("Calculadora - Guarda en Oracle 19c");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(360, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(6,6));

        dao = new ResultDAO();
        currentExpression = new StringBuilder();

        // Display
        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("SansSerif", Font.BOLD, 24));
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        add(display, BorderLayout.NORTH);

        // Panel de botones
        JPanel panel = new JPanel(new GridLayout(5, 4, 6, 6));
        String[] botones = {
                "7","8","9","/",
                "4","5","6","*",
                "1","2","3","-",
                "0",".","=","+",
                "C","←","Guardar","Historial"
        };
        for (String b : botones) {
            JButton btn = new JButton(b);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 18));
            btn.addActionListener(new ButtonListener());
            panel.add(btn);
        }
        add(panel, BorderLayout.CENTER);

        setVisible(true);
    }

    private class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            switch (cmd) {
                case "C": currentExpression.setLength(0); display.setText(""); break;
                case "←":
                    if (currentExpression.length() > 0) {
                        currentExpression.setLength(currentExpression.length() - 1);
                        display.setText(currentExpression.toString());
                    }
                    break;
                case "=": evaluateAndShow(); break;
                case "Guardar": saveCurrent(); break;
                case "Historial": showHistory(); break;
                default:
                    currentExpression.append(cmd);
                    display.setText(currentExpression.toString());
            }
        }

        private void evaluateAndShow() {
            String expr = currentExpression.toString().trim();
            if (expr.isEmpty()) return;
            try {
                double value = ExpressionEvaluator.evaluate(expr);
                String resultado = formatResult(value);
                display.setText(resultado);
                currentExpression.setLength(0);
                currentExpression.append(resultado);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(CalculatorGUI.this, "Expresión inválida: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void saveCurrent() {
            String expr = display.getText().trim();
            if (expr.isEmpty()) expr = currentExpression.toString().trim();
            if (expr.isEmpty()) {
                JOptionPane.showMessageDialog(CalculatorGUI.this, "No hay nada para guardar.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String expresion = currentExpression.toString();
            String resultado = expr;
            try {
                dao.save(expresion, resultado);
                JOptionPane.showMessageDialog(CalculatorGUI.this, "Registro guardado en la base de datos.", "OK", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(CalculatorGUI.this, "Error al guardar en la BD: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showHistory() {
            StringBuilder sb = new StringBuilder();
            try (var conn = DatabaseManager.getConnection();
                 var st = conn.createStatement();
                 var rs = st.executeQuery("SELECT id, expresion, resultado, fecha FROM calculos ORDER BY fecha DESC FETCH FIRST 50 ROWS ONLY")) {
                while (rs.next()) {
                    sb.append(rs.getLong("id"))
                            .append(": ")
                            .append(rs.getString("expresion"))
                            .append(" = ")
                            .append(rs.getString("resultado"))
                            .append(" (")
                            .append(rs.getTimestamp("fecha"))
                            .append(")\n");
                }
                JTextArea area = new JTextArea(sb.length() == 0 ? "No hay registros." : sb.toString());
                area.setEditable(false);
                JScrollPane sp = new JScrollPane(area);
                sp.setPreferredSize(new Dimension(600, 400));
                JOptionPane.showMessageDialog(CalculatorGUI.this, sp, "Historial (últimos 50)", JOptionPane.PLAIN_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(CalculatorGUI.this, "Error al leer historial: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            }
        }

        private String formatResult(double value) {
            return (Math.abs(value - Math.round(value)) < 1e-12)
                    ? String.valueOf((long) Math.round(value))
                    : String.valueOf(value);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CalculatorGUI::new);
    }
}
