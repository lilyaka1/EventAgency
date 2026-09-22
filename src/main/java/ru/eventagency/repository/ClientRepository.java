package ru.eventagency.repository;

import ru.eventagency.model.Client;
import ru.eventagency.util.DatabaseManager;
import java.sql.*;
import java.util.*;

public class ClientRepository implements Repository<Client> {
    private final DatabaseManager db;
    public ClientRepository(DatabaseManager db) { this.db = db; }
    private Client map(ResultSet rs) throws SQLException { return new Client(rs.getInt("id"), rs.getString("full_name"), rs.getString("email"), rs.getString("phone")); }
    public Client create(Client c) {
        String sql = "INSERT INTO clients(full_name,email,phone) VALUES(?,?,?) RETURNING id";
        try (Connection con=db.getConnection(); PreparedStatement ps=con.prepareStatement(sql)) { ps.setString(1,c.getFullName()); ps.setString(2,c.getEmail()); ps.setString(3,c.getPhone()); try(ResultSet rs=ps.executeQuery()){rs.next(); c.setId(rs.getInt(1)); return c;} } catch(SQLException e){throw new RuntimeException("Ошибка создания клиента: "+e.getMessage(),e);}
    }
    public List<Client> findAll() { List<Client> result=new ArrayList<>(); try(Connection con=db.getConnection(); PreparedStatement ps=con.prepareStatement("SELECT * FROM clients ORDER BY id"); ResultSet rs=ps.executeQuery()){while(rs.next())result.add(map(rs)); return result;}catch(SQLException e){throw new RuntimeException("Ошибка чтения клиентов: "+e.getMessage(),e);} }
    public Optional<Client> findById(int id) { try(Connection con=db.getConnection(); PreparedStatement ps=con.prepareStatement("SELECT * FROM clients WHERE id=?")){ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?Optional.of(map(rs)):Optional.empty();}}catch(SQLException e){throw new RuntimeException("Ошибка поиска клиента: "+e.getMessage(),e);} }
    public void update(Client c) { String sql="UPDATE clients SET full_name=?,email=?,phone=? WHERE id=?"; try(Connection con=db.getConnection();PreparedStatement ps=con.prepareStatement(sql)){ps.setString(1,c.getFullName());ps.setString(2,c.getEmail());ps.setString(3,c.getPhone());ps.setInt(4,c.getId()); if(ps.executeUpdate()==0)throw new RuntimeException("Клиент не найден");}catch(SQLException e){throw new RuntimeException("Ошибка изменения клиента: "+e.getMessage(),e);} }
    public void delete(int id) { try(Connection con=db.getConnection();PreparedStatement ps=con.prepareStatement("DELETE FROM clients WHERE id=?")){ps.setInt(1,id);if(ps.executeUpdate()==0)throw new RuntimeException("Клиент не найден");}catch(SQLException e){throw new RuntimeException("Ошибка удаления клиента: "+e.getMessage(),e);} }
    public List<Client> search(String text) { List<Client> result=new ArrayList<>(); String sql="SELECT * FROM clients WHERE LOWER(full_name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?) ORDER BY full_name"; try(Connection con=db.getConnection();PreparedStatement ps=con.prepareStatement(sql)){String p="%"+text+"%";ps.setString(1,p);ps.setString(2,p);try(ResultSet rs=ps.executeQuery()){while(rs.next())result.add(map(rs));return result;}}catch(SQLException e){throw new RuntimeException("Ошибка поиска: "+e.getMessage(),e);} }
}
