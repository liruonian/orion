package liruonian.orion.core.engine;

import liruonian.orion.Valve;
import liruonian.orion.ValveChain;

/**
 * 标准的业务处理管道实现
 *
 *
 * @author lihao
 * @date 2020年8月27日
 * @version 1.0
 */
public class StandardPipeline implements Pipeline {

    private Valve basic;
    private Valve[] valves = new Valve[0];

    /*
     * @see liruonian.orion.Pipeline#getBasic()
     */
    @Override
    public Valve getBasic() {
        return basic;
    }

    /*
     * @see liruonian.orion.Pipeline#setBasic(liruonian.orion.Valve)
     */
    @Override
    public void setBasic(Valve basic) {
        this.basic = basic;
    }

    /*
     * @see liruonian.orion.Pipeline#addValve(liruonian.orion.Valve)
     */
    @Override
    public void addValve(Valve valve) {
        Valve results[] = new Valve[valves.length + 1];
        System.arraycopy(valves, 0, results, 0, valves.length);
        results[valves.length] = valve;
        valves = results;
    }

    /*
     * @see liruonian.orion.Pipeline#getValves()
     */
    @Override
    public Valve[] getValves() {
        Valve results[] = new Valve[valves.length + 1];
        System.arraycopy(valves, 0, results, 0, valves.length);
        results[valves.length] = basic;
        return (results);
    }

    /*
     * @see liruonian.orion.Pipeline#removeValve(liruonian.orion.Valve)
     */
    @Override
    public void removeValve(Valve valve) {
        int j = -1;
        for (int i = 0; i < valves.length; i++) {
            if (valve == valves[i]) {
                j = i;
                break;
            }
        }
        if (j < 0) {
            return;
        }

        Valve results[] = new Valve[valves.length - 1];
        int k = 0;
        for (int i = 0; i < valves.length; i++) {
            if (i == j) {
                continue;
            }
            results[k++] = valves[i];
        }
        valves = results;
    }

    /*
     * @see liruonian.orion.Pipeline#invoke(liruonian.orion.RequestFaced,
     * liruonian.orion.ResponseFaced, liruonian.orion.Invocation)
     */
    @Override
    public void invoke(RequestFaced request, ResponseFaced response,
            Invocation invocation) {
        new StandardPipelineValveChain().invokeNext(request, response, invocation);
    }

    /**
     * valve chain，先执行<code>valves</code>中的{@link Valve}，
     * 最后执行<code>basic</code>
     *
     *
     * @author lihao
     * @date 2020年8月29日 上午10:36:09
     * @version 1.0
     *
     */
    private class StandardPipelineValveChain implements ValveChain {

        private int stage = 0;

        /*
         * @see
         * liruonian.orion.ValveChain#invokeNext(liruonian.orion.RequestFaced,
         * liruonian.orion.ResponseFaced, liruonian.orion.Invocation)
         */
        @Override
        public void invokeNext(RequestFaced request, ResponseFaced response,
                Invocation invocation) {
            int subscript = stage;
            stage = stage + 1;

            if (subscript < valves.length) {
                valves[subscript].invoke(request, response, invocation, this);
            } else if (subscript == valves.length && basic != null) {
                basic.invoke(request, response, invocation, this);
            }
        }

    }
}
