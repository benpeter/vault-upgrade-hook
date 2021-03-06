/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler.slingpipes;

import java.nio.charset.StandardCharsets;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallContext.Phase;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.pipes.Pipe;
import org.apache.sling.pipes.Plumber;

import biz.netcentric.vlt.upgrade.UpgradeAction;
import biz.netcentric.vlt.upgrade.handler.OsgiUtil;
import biz.netcentric.vlt.upgrade.handler.OsgiUtil.ServiceWrapper;
import biz.netcentric.vlt.upgrade.util.PackageInstallLogger;
import biz.netcentric.vlt.upgrade.util.JsonResourceSerializer;

public class SlingPipe extends UpgradeAction {

    private static final PackageInstallLogger LOG = PackageInstallLogger.create(SlingPipe.class);

    OsgiUtil osgi = new OsgiUtil();

    private final Resource resource;

    public SlingPipe(Resource resource, Phase defaultPhase) {
        super(resource.getName(), UpgradeAction.getPhaseFromPrefix(defaultPhase, resource.getName()),
                getMd5(new JsonResourceSerializer().serialize(resource), StandardCharsets.UTF_8.name()));
        this.resource = resource;
    }

    @Override
    public void execute(InstallContext ctx) {
        try (ServiceWrapper<Plumber> serviceWrapper = osgi.getService(Plumber.class)) {
            Pipe pipe = serviceWrapper.getService().getPipe(resource);
            if (pipe == null) {
                throw new IllegalArgumentException("No valid pipe at " + resource);
            }
            LOG.debug(ctx, "Executing [{}]: [{}]", resource.getName(), pipe);

            for (Resource r : JcrUtils.in(pipe.getOutput())) {
                // output affected resource path for information
                LOG.info(ctx, r.getPath());
            }
        }
    }

}
