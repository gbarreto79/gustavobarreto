// ===== TEMA CLARO / ESCURO =====
document.addEventListener("DOMContentLoaded", () => {

    const toggleBtn = document.getElementById("toggleTheme");

    if (toggleBtn) {

        // Carregar tema salvo
        if (localStorage.getItem("theme") === "dark") {
            document.body.classList.add("dark");
            toggleBtn.textContent = "☀️";
        } else {
            toggleBtn.textContent = "🌙";
        }

        toggleBtn.addEventListener("click", () => {
            document.body.classList.toggle("dark");

            if (document.body.classList.contains("dark")) {
                toggleBtn.textContent = "☀️";
                localStorage.setItem("theme", "dark");
            } else {
                toggleBtn.textContent = "🌙";
                localStorage.setItem("theme", "light");
            }
        });
    }

});

// FORMULÁRIO DE CONTATO COM EMAILJS
document.getElementById("formContato").addEventListener("submit", function(e) {
    e.preventDefault();

    const nome = document.getElementById("nome").value.trim();
    const email = document.getElementById("email").value.trim();
    const mensagem = document.getElementById("mensagem").value.trim();
    const status = document.getElementById("status");

    if (!nome || !email || !mensagem) {
        alert("Preencha todos os campos.");
        return;
    }

    const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!regexEmail.test(email)) {
        alert("E-mail inválido.");
        return;
    }

    const params = {
        name: nome,
        email: email,
        message: mensagem
    };

    emailjs.send("service_hh1o172", "template_e7ahsec", params)
        .then(() => {
            status.innerHTML = "Mensagem enviada com sucesso!";
            document.getElementById("formContato").reset();
        })
        .catch((err) => {
            status.innerHTML = "Erro ao enviar. Tente novamente.";
            console.error("Erro ao enviar email:", err);
        });
});
