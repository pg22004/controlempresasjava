package controlempresas.accesoadatos;

import java.util.*;
import java.sql.*;
import controlempresas.entidadesdenegocio.*;

public class RolDAL {
    //metodo estatico que devuelve un string (campos de la tabla)
     static String obtenerCampos() {
        return "r.Id, r.Nombre";
    }
    //se fuciona con el metodo anterior
     
    private static String obtenerSelect(Rol pRol) {
        String sql;
        sql = "SELECT ";
        //si se 
        if (pRol.getTop_aux() > 0 && ComunDB.TIPODB == ComunDB.TipoDB.SQLSERVER) {            
            sql += "TOP " + pRol.getTop_aux() + " ";
        }
        sql += (obtenerCampos() + " FROM Rol r");
        return sql;
    }
    
    private static String agregarOrderBy(Rol pRol) {
        //los registros se ordenan en forma decendente
        String sql = " ORDER BY r.Id DESC";
        if (pRol.getTop_aux() > 0 && ComunDB.TIPODB == ComunDB.TipoDB.MYSQL) {
            sql += " LIMIT " + pRol.getTop_aux() + " ";
        }
        return sql;
    }
    //lleva consultas sql que se utilizaran
   
    public static int crear(Rol pRol) throws Exception {
        //variable donde se almacenara
        int result;
        String sql;
         //manejo de excepciones, seguido de abrir la conexion 
        try (Connection conn = ComunDB.obtenerConexion();) { 
            //? enviar un parametro
            sql = "INSERT INTO Rol(Nombre) VALUES(?)";
            //variable prepared... que se ejecuta cuando una consulta no lleva parametros
            try (PreparedStatement ps = ComunDB.createPreparedStatement(conn, sql);) {
                ps.setString(1, pRol.getNombre());
                //actualizacion, update
                result = ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                throw ex;
            }
            conn.close();
        } catch (SQLException ex) {
            throw ex;
        }
        return result;
    }
    
    public static int modificar(Rol pRol) throws Exception {
        int result;
        String sql;
        try (Connection conn = ComunDB.obtenerConexion();) {
            sql = "UPDATE Rol SET Nombre=? WHERE Id=?";
            try (PreparedStatement ps = ComunDB.createPreparedStatement(conn, sql);) {
                //dependiendo del tipo de dato
                ps.setString(1, pRol.getNombre());
                ps.setInt(2, pRol.getId());
                result = ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                throw ex;
            }
            conn.close();
        } catch (SQLException ex) {
            throw ex;
        }
        return result;
    }
    
    public static int eliminar(Rol pRol) throws Exception {
        int result;
        String sql;
        try (Connection conn = ComunDB.obtenerConexion();) {
            //consulta de eliminar 
            sql = "DELETE FROM Rol WHERE Id=?";
            try (PreparedStatement ps = ComunDB.createPreparedStatement(conn, sql);) {
                ps.setInt(1, pRol.getId());
                result = ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                throw ex;
            }
            conn.close();
        } catch (SQLException ex) {
            throw ex;
        }
        return result;
    } 
    
    //metodo para 
    //va hasta la bd, y lleva datos de las tablas
    
    static int asignarDatosResultSet(Rol pRol, ResultSet pResultSet, int pIndex) throws Exception {
        //se incrementa el valor en 1 
        pIndex++;
        pRol.setId(pResultSet.getInt(pIndex));
        pIndex++;
        //asigancion del valor
        pRol.setNombre(pResultSet.getString(pIndex));
        return pIndex;
    }
    
    
    private static void obtenerDatos(PreparedStatement pPS, ArrayList<Rol> pRoles) throws Exception {
        try (ResultSet resultSet = ComunDB.obtenerResultSet(pPS);) {
            while (resultSet.next()) {
                Rol rol = new Rol(); 
                asignarDatosResultSet(rol, resultSet, 0);
                pRoles.add(rol);
            }
            resultSet.close();
        } catch (SQLException ex) {
            throw ex;
        }
    }
    
    public static Rol obtenerPorId(Rol pRol) throws Exception {
        Rol rol = new Rol();
        //declarar lista
        ArrayList<Rol> roles = new ArrayList();
        try (Connection conn = ComunDB.obtenerConexion();) { 
            //declaracion de variable sql
            String sql = obtenerSelect(pRol);
            //where especifica condicion, es decir que el id coincida con el dato que se modificara
            sql += " WHERE r.Id=?";
            try (PreparedStatement ps = ComunDB.createPreparedStatement(conn, sql);) {
                ps.setInt(1, pRol.getId());
                 //se manda a ejecutar
                obtenerDatos(ps, roles);
                ps.close();
            } catch (SQLException ex) {
                throw ex;
            }
            conn.close();
        }
        catch (SQLException ex) {
            throw ex;
        }
        
        if (roles.size() > 0) {
            rol = roles.get(0);
        }
        
        return rol;
    }
    
    public static ArrayList<Rol> obtenerTodos() throws Exception {
        ArrayList<Rol> roles = new ArrayList<>();
        try (Connection conn = ComunDB.obtenerConexion();) {
            String sql = obtenerSelect(new Rol());
            sql += agregarOrderBy(new Rol());
            try (PreparedStatement ps = ComunDB.createPreparedStatement(conn, sql);) {
                //se manda a ejecutar
                obtenerDatos(ps, roles);
                ps.close();
            } catch (SQLException ex) {
                throw ex;
            }
            conn.close();
        } 
        catch (SQLException ex) {
            throw ex;
        }
        
        return roles;
    }
    
    //arma las consultas
    static void querySelect(Rol pRol, ComunDB.utilQuery pUtilQuery) throws SQLException {
        PreparedStatement statement = pUtilQuery.getStatement();
        if (pRol.getId() > 0) {
            pUtilQuery.AgregarNumWhere(" r.Id=? ");
            if (statement != null) { 
                statement.setInt(pUtilQuery.getNumWhere(), pRol.getId()); 
            }
        }

        if (pRol.getNombre() != null && pRol.getNombre().trim().isEmpty() == false) {
            pUtilQuery.AgregarNumWhere(" r.Nombre LIKE ? "); 
            if (statement != null) {
                statement.setString(pUtilQuery.getNumWhere(), "%" + pRol.getNombre() + "%"); 
            }
        }
    }
    
    public static ArrayList<Rol> buscar(Rol pRol) throws Exception {
        ArrayList<Rol> roles = new ArrayList();
        try (Connection conn = ComunDB.obtenerConexion();) {
            String sql = obtenerSelect(pRol);
            ComunDB comundb = new ComunDB();
            ComunDB.utilQuery utilQuery = comundb.new utilQuery(sql, null, 0); 
            querySelect(pRol, utilQuery);
            sql = utilQuery.getSQL(); 
            sql += agregarOrderBy(pRol);
            try (PreparedStatement ps = ComunDB.createPreparedStatement(conn, sql);) {
                utilQuery.setStatement(ps);
                utilQuery.setSQL(null);
                utilQuery.setNumWhere(0); 
                querySelect(pRol, utilQuery);
                obtenerDatos(ps, roles);
                ps.close();
            } catch (SQLException ex) {
                throw ex;
            }
            conn.close();
        }
        catch (SQLException ex) {
            throw ex;
        }
        return roles;
    }
}
