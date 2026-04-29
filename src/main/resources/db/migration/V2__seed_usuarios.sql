-- =====================================================================
-- SEED INICIAL DE USUARIOS DEL SISTEMA
-- BCrypt cost 10 — generado con BCryptPasswordEncoder(10)
-- =====================================================================

INSERT INTO usuarios (nombre, password_hash, rol, activo)
VALUES
    ('admin',     '$2a$10$xNp2a3q6xRGMdamwO2eZFuTtiuRuJmUqMRnf2PeT/mv22XVman0RO', 'admin',     TRUE),
    ('mesero',    '$2a$10$LJgDHcDwZWQaJTluomtEr.XLFv0JY9VGqmB9eLuYIdedxoQhgReGG', 'mesero',    TRUE),
    ('bartender', '$2a$10$vjxygaG/hywpo2jfycjMNOH7PPGG2WUJnFLg3F.rkK9PTA3RKK01.', 'bartender', TRUE)
ON CONFLICT DO NOTHING;
