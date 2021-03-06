/***
 * 
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * copyright holders nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.caelum.integracao.server.ioc;

import br.com.caelum.integracao.LongMultipartConfig;
import br.com.caelum.integracao.server.Builds;
import br.com.caelum.integracao.server.Projects;
import br.com.caelum.integracao.server.agent.Clients;
import br.com.caelum.integracao.server.dao.DatabaseFactory;
import br.com.caelum.integracao.server.label.Labels;
import br.com.caelum.integracao.server.logic.PingScmThread;
import br.com.caelum.integracao.server.queue.Jobs;
import br.com.caelum.integracao.server.queue.QueueThread;
import br.com.caelum.integracao.server.vraptor.PathResolver;
import br.com.caelum.vraptor.ComponentRegistry;
import br.com.caelum.vraptor.http.route.RoutesConfiguration;
import br.com.caelum.vraptor.interceptor.multipart.MultipartConfig;
import br.com.caelum.vraptor.ioc.pico.PicoProvider;

public class CustomProvider extends PicoProvider {

    @Override
    protected void registerComponents(ComponentRegistry container) {
        super.registerComponents(container);
        container.register(DatabaseFactory.class, DatabaseFactory.class);
        container.register(Clients.class, Clients.class);
        container.register(Projects.class, Projects.class);
        container.register(Jobs.class, Jobs.class);
        container.register(br.com.caelum.vraptor.view.PathResolver.class, PathResolver.class);
        container.register(PingScmThread.class, PingScmThread.class);
        container.register(QueueThread.class, QueueThread.class);
        container.register(Builds.class, Builds.class);
        container.register(Labels.class, Labels.class);
        container.register(RoutesConfiguration.class, CustomRoutes.class);
		container.register(MultipartConfig.class, LongMultipartConfig.class);
    }

}
